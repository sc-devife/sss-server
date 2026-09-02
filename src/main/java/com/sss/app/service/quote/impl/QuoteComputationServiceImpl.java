package com.sss.app.service.quote.impl;

import com.sss.app.dto.quote.PricingBreakdownDTO;
import com.sss.app.dto.quote.QuoteComputeRequestDTO;
import com.sss.app.dto.quote.QuoteComputeResponseDTO;
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
 * back to the library Activity's base_price); Transport reads its own
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
            BigDecimal price = resolvePrice(item, warnings);
            if (price != null) {
                subtotal = subtotal.add(price);
                switch (item.getItemType()) {
                    case "hotel" -> breakdown.setHotelsInr(breakdown.getHotelsInr().add(price));
                    case "activity" -> breakdown.setActivitiesInr(breakdown.getActivitiesInr().add(price));
                    case "transport" -> breakdown.setTransportInr(breakdown.getTransportInr().add(price));
                    default -> breakdown.setOtherInr(breakdown.getOtherInr().add(price));
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

        BigDecimal total = subtotal.add(taxAmount).subtract(discountAmount);
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
        quote.setDiscountType(discountType);
        quote.setDiscountValue(discountValue);
        quote.setTotalInr(total.setScale(2, RoundingMode.HALF_UP));
        quote.setCurrencyCode(request.getDisplayCurrencyCode());
        quote.setFxRateSnapshot(request.getFxRateSnapshot());
        Quote saved = quoteRepository.save(quote);

        QuoteComputeResponseDTO response = new QuoteComputeResponseDTO();
        response.setQuote(quoteMapper.toResponse(saved));
        response.setPricingWarnings(warnings);
        response.setDisplayTotal(displayTotal);
        response.setBreakdown(breakdown);
        return response;
    }

    private BigDecimal resolvePrice(ItineraryItem item, List<String> warnings) {
        switch (item.getItemType()) {
            case "activity" -> {
                // This specific booking's own price wins over the library's
                // generic default — an agent may well have negotiated or
                // overridden it for this itinerary.
                if (item.getPrice() != null) {
                    return item.getPrice();
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
                return activity.getBasePrice();
            }
            case "transport" -> {
                ItineraryItemTransportDetail detail = transportDetailRepository.findByItineraryItem_Seqp(item.getSeqp()).orElse(null);
                BigDecimal detailPrice = resolveTransportDetailPrice(detail);
                if (detailPrice != null) {
                    return detailPrice;
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
                return transport.getBasePrice();
            }
            case "hotel" -> {
                ItineraryItemHotelDetail detail = hotelDetailRepository.findByItineraryItem_Seqp(item.getSeqp()).orElse(null);
                if (detail == null) {
                    warnings.add("Hotel on day " + item.getDayNumber() + " has no booking details set — excluded");
                    return null;
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
                return stayPrice.add(inclusionsTotal);
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
