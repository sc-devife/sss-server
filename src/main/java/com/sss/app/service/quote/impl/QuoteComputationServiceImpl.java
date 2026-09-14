package com.sss.app.service.quote.impl;

import com.sss.app.dto.quote.PricingBreakdownDTO;
import com.sss.app.dto.quote.QuoteComputeRequestDTO;
import com.sss.app.dto.quote.QuoteComputeResponseDTO;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.itinerary.ItineraryItemHotelDetail;
import com.sss.app.entity.itinerary.ItineraryItemHotelInclusion;
import com.sss.app.entity.itinerary.ItineraryItemTransportDetail;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.taxprofile.TaxProfile;
import com.sss.app.exception.BadRequestException;
import com.sss.app.helper.quote.QuoteHelper;
import com.sss.app.helper.taxprofile.TaxProfileHelper;
import com.sss.app.mapper.quote.QuoteMapper;
import com.sss.app.repository.itinerary.ItineraryItemHotelDetailRepository;
import com.sss.app.repository.itinerary.ItineraryItemHotelInclusionRepository;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
import com.sss.app.repository.itinerary.ItineraryItemTransportDetailRepository;
import com.sss.app.repository.library.activity.ActivityRepository;
import com.sss.app.repository.library.transport.TransportRepository;
import com.sss.app.repository.quote.QuoteRepository;
import com.sss.app.service.quote.QuoteComputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Section 6 Quotation Engine — sums pricing from itinerary items, preferring
 * whatever price was actually agreed for that specific booking over the
 * library's generic default: Activity reads its own item.price (falling
 * back to the library Activity's base_price), times its own travelers_count
 * (defaulting to 1); Transport reads its own
 * booking-detail row's sellingPrice (x pax count when marked per-person) or
 * flat price (falling back to the library Transport's base_price); Hotel
 * reads its own booking-detail row (total_price, or price x room_count when
 * total_price wasn't set) plus any add-on services
 * (ItineraryItemHotelInclusion) attached to that stay. A custom (non-library)
 * item has no base_price to fall back to, so it's excluded if its own price
 * was never filled in. Tax comes from a real, org-configurable TaxProfile.
 * FX conversion uses a manually-entered, frozen rate rather than a live
 * provider (no FX rate API integration has been requested/authorized yet).
 * Base/storage currency is INR — a display currency other than INR requires
 * an FX rate snapshot.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuoteComputationServiceImpl implements QuoteComputationService {

    private final QuoteHelper quoteHelper;
    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;
    private final TaxProfileHelper taxProfileHelper;
    private final ItineraryItemRepository itineraryItemRepository;
    private final ActivityRepository activityRepository;
    private final TransportRepository transportRepository;
    private final ItineraryItemHotelDetailRepository hotelDetailRepository;
    private final ItineraryItemHotelInclusionRepository hotelInclusionRepository;
    private final ItineraryItemTransportDetailRepository transportDetailRepository;

    @Override
    public QuoteComputeResponseDTO compute(UUID quoteUid, QuoteComputeRequestDTO request) {
        Quote quote = quoteHelper.getByUid(quoteUid);

        List<ItineraryItem> items = itineraryItemRepository
                .findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(quote.getItinerary().getSeqp());

        List<String> warnings = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        PricingBreakdownDTO breakdown = new PricingBreakdownDTO();

        for (ItineraryItem item : items) {
            ItemPriceResult result = resolvePrice(item, warnings);
            if (result != null) {
                subtotal = subtotal.add(result.amount());
                if (result.isCancellation()) {
                    breakdown.setCancellationInr(breakdown.getCancellationInr().add(result.amount()));
                } else {
                    switch (item.getItemType()) {
                        case "hotel" -> breakdown.setHotelsInr(breakdown.getHotelsInr().add(result.amount()));
                        case "activity" -> breakdown.setActivitiesInr(breakdown.getActivitiesInr().add(result.amount()));
                        case "transport" -> breakdown.setTransportInr(breakdown.getTransportInr().add(result.amount()));
                        default -> breakdown.setOtherInr(breakdown.getOtherInr().add(result.amount()));
                    }
                }
            }
        }

        BigDecimal taxAmount = BigDecimal.ZERO;
        UUID taxProfileUid = null;
        if (request.getTaxProfileUid() != null) {
            TaxProfile taxProfile = taxProfileHelper.getByUid(request.getTaxProfileUid());
            taxAmount = subtotal.multiply(taxProfile.getRatePercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            taxProfileUid = taxProfile.getUid();
        }

        // TCS is levied on the customer-facing package price — i.e. subtotal
        // plus GST — not on the pre-tax subtotal alone, matching how
        // outbound-tour-package TCS is actually charged in practice.
        BigDecimal tcsRatePercent = request.getTcsRatePercent();
        BigDecimal tcsAmount = tcsRatePercent != null
                ? subtotal.add(taxAmount).multiply(tcsRatePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String discountType = request.getDiscountType() != null ? request.getDiscountType() : "none";
        BigDecimal discountValue = request.getDiscountValue();
        BigDecimal discountAmount = switch (discountType) {
            case "percent" -> discountValue != null
                    ? subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            case "flat" -> discountValue != null ? discountValue : BigDecimal.ZERO;
            case "none" -> BigDecimal.ZERO;
            default -> throw new BadRequestException("discountType must be one of: none, percent, flat");
        };

        BigDecimal total = subtotal.add(taxAmount).add(tcsAmount).subtract(discountAmount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        BigDecimal displayTotal = null;
        if (request.getDisplayCurrencyCode() != null && !"INR".equalsIgnoreCase(request.getDisplayCurrencyCode())) {
            if (request.getFxRateSnapshot() == null) {
                throw new BadRequestException("fxRateSnapshot is required when displayCurrencyCode is not INR");
            }
            displayTotal = total.multiply(request.getFxRateSnapshot()).setScale(2, RoundingMode.HALF_UP);
        }

        quote.setSubtotalInr(subtotal.setScale(2, RoundingMode.HALF_UP));
        quote.setTaxProfileId(taxProfileUid);
        quote.setTaxAmountInr(taxAmount);
        quote.setTcsRatePercent(tcsRatePercent);
        quote.setTcsAmountInr(tcsAmount);
        quote.setDiscountType(discountType);
        quote.setDiscountValue(discountValue);
        quote.setTotalInr(total.setScale(2, RoundingMode.HALF_UP));
        quote.setCancellationChargesInr(breakdown.getCancellationInr());
        quote.setCurrencyCode(request.getDisplayCurrencyCode());
        quote.setFxRateSnapshot(request.getFxRateSnapshot());
        Quote saved = quoteRepository.save(quote);

        int paxCount = quote.getItinerary().getEscape().getTravellers() != null
                ? quote.getItinerary().getEscape().getTravellers().size()
                : 0;

        QuoteComputeResponseDTO response = new QuoteComputeResponseDTO();
        response.setQuote(quoteMapper.toResponse(saved));
        response.setPricingWarnings(warnings);
        response.setDisplayTotal(displayTotal);
        response.setBreakdown(breakdown);
        response.setPaxCount(paxCount);
        response.setPerPaxInr(paxCount > 0 ? total.divide(BigDecimal.valueOf(paxCount), 2, RoundingMode.HALF_UP) : null);
        return response;
    }

    // isCancellation flags a Dropped hotel's cancellation charge — the
    // caller buckets these into PricingBreakdownDTO.cancellationInr instead
    // of hotelsInr, so an active-hotel total and a cancellation total never
    // get mixed together.
    private record ItemPriceResult(BigDecimal amount, boolean isCancellation) {
        private static ItemPriceResult of(BigDecimal amount) {
            return new ItemPriceResult(amount, false);
        }
    }

    private ItemPriceResult resolvePrice(ItineraryItem item, List<String> warnings) {
        switch (item.getItemType()) {
            case "activity" -> {
                // A dropped activity is no longer priced as an active
                // booking — same rule as a dropped hotel (see the "hotel"
                // branch below): its only contribution is whatever
                // cancellation charge was actually levied (zero if none was
                // entered), regardless of the original price.
                if (BookingStatus.DROP.equals(item.getStatus())) {
                    BigDecimal charge = item.getCancellationChargeInr() != null
                            ? item.getCancellationChargeInr()
                            : BigDecimal.ZERO;
                    return new ItemPriceResult(charge, true);
                }
                // This specific booking's own price wins over the library's
                // generic default — an agent may well have negotiated or
                // overridden it for this itinerary. Either way, price is a
                // per-traveller rate — multiplied by this booking's own
                // travellers count (defaulting to 1 for items saved before
                // that field existed) to get the actual total contribution.
                BigDecimal travellersMultiplier = BigDecimal.valueOf(
                        item.getTravelersCount() != null && item.getTravelersCount() > 0 ? item.getTravelersCount() : 1);
                if (item.getPrice() != null) {
                    return ItemPriceResult.of(item.getPrice().multiply(travellersMultiplier));
                }
                if (item.getReferenceId() == null) {
                    warnings.add("Custom activity on day " + item.getDayNumber() + " has no price set — excluded");
                    return null;
                }
                var activity = activityRepository.findByUid(item.getReferenceId()).orElse(null);
                if (activity == null) {
                    warnings.add("An activity referenced on day " + item.getDayNumber() + " no longer exists — excluded");
                    return null;
                }
                if (activity.getBasePrice() == null) {
                    warnings.add("Activity \"" + activity.getName() + "\" (day " + item.getDayNumber() + ") has no price set — excluded");
                    return null;
                }
                return ItemPriceResult.of(activity.getBasePrice().multiply(travellersMultiplier));
            }
            case "transport" -> {
                ItineraryItemTransportDetail detail = transportDetailRepository.findByItineraryItem_Seqp(item.getSeqp()).orElse(null);
                BigDecimal detailPrice = resolveTransportDetailPrice(detail);
                if (detailPrice != null) {
                    return ItemPriceResult.of(detailPrice);
                }
                if (item.getReferenceId() == null) {
                    warnings.add("Custom transport on day " + item.getDayNumber() + " has no price set — excluded");
                    return null;
                }
                var transport = transportRepository.findByUid(item.getReferenceId()).orElse(null);
                if (transport == null) {
                    warnings.add("A transport item referenced on day " + item.getDayNumber() + " no longer exists — excluded");
                    return null;
                }
                if (transport.getBasePrice() == null) {
                    warnings.add("Transport on day " + item.getDayNumber() + " has no price set — excluded");
                    return null;
                }
                return ItemPriceResult.of(transport.getBasePrice());
            }
            case "hotel" -> {
                ItineraryItemHotelDetail detail = hotelDetailRepository.findByItineraryItem_Seqp(item.getSeqp()).orElse(null);
                if (detail == null) {
                    warnings.add("Hotel on day " + item.getDayNumber() + " has no booking details set — excluded");
                    return null;
                }
                // A dropped stay is no longer priced as an active hotel
                // booking — its only contribution is whatever cancellation
                // charge the hotel actually levied (zero if none was
                // entered), regardless of what the original room price was.
                if (BookingStatus.DROP.equals(detail.getStatus())) {
                    BigDecimal charge = detail.getCancellationChargeInr() != null
                            ? detail.getCancellationChargeInr()
                            : BigDecimal.ZERO;
                    return new ItemPriceResult(charge, true);
                }
                BigDecimal stayPrice = detail.getTotalPrice() != null
                        ? detail.getTotalPrice()
                        : (detail.getPrice() != null && detail.getRoomCount() != null
                                ? detail.getPrice().multiply(BigDecimal.valueOf(detail.getRoomCount()))
                                : null);
                if (stayPrice == null) {
                    warnings.add("Hotel on day " + item.getDayNumber() + " has no price or room count set — excluded");
                    return null;
                }
                BigDecimal inclusionsTotal = hotelInclusionRepository
                        .findAllByItineraryItem_SeqpOrderBySeqpAsc(item.getSeqp()).stream()
                        .map(ItineraryItemHotelInclusion::getTotalPrice)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                return ItemPriceResult.of(stayPrice.add(inclusionsTotal));
            }
            default -> {
                return null;
            }
        }
    }

    // Selling price (what the customer is charged) wins over the flat
    // price, since a flight's detail form only ever populates one or the
    // other (see TransportDetailFields.tsx: cost/selling split is
    // flight-only, every other mode uses the plain flat price). Cost price
    // is deliberately never read here — it's the agency's internal margin
    // figure, not something that belongs in a customer-facing quote.
    private BigDecimal resolveTransportDetailPrice(ItineraryItemTransportDetail detail) {
        if (detail == null) {
            return null;
        }
        if (detail.getSellingPrice() != null) {
            int totalPax = nullToZero(detail.getAdultsCount()) + nullToZero(detail.getChildrenCount()) + nullToZero(detail.getInfantsCount());
            int payingPax = Math.max(totalPax, 1);
            BigDecimal multiplier = Boolean.TRUE.equals(detail.getSellingPricePerPerson())
                    ? BigDecimal.valueOf(payingPax)
                    : BigDecimal.ONE;
            return detail.getSellingPrice().multiply(multiplier);
        }
        return detail.getPrice();
    }

    private int nullToZero(Integer value) {
        return value != null ? value : 0;
    }
}
