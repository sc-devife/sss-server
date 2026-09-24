package com.sss.app.service.quote.impl;

import com.sss.app.dto.quote.PricingBreakdownDTO;
import com.sss.app.dto.quote.QuoteComputeRequestDTO;
import com.sss.app.dto.quote.QuoteComputeResponseDTO;
import com.sss.app.dto.quote.QuoteLineItemDiscountUpdateRequestDTO;
import com.sss.app.dto.quote.QuoteLineItemsResponseDTO;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.itinerary.ItineraryItemHotelDetail;
import com.sss.app.entity.itinerary.ItineraryItemHotelInclusion;
import com.sss.app.entity.itinerary.ItineraryItemTransportDetail;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.quote.QuoteLineItem;
import com.sss.app.entity.taxprofile.TaxProfile;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.itinerary.ItineraryItemHelper;
import com.sss.app.helper.quote.QuoteHelper;
import com.sss.app.helper.taxprofile.TaxProfileHelper;
import com.sss.app.mapper.quote.QuoteLineItemMapper;
import com.sss.app.mapper.quote.QuoteResponseAssembler;
import com.sss.app.repository.itinerary.ItineraryItemHotelDetailRepository;
import com.sss.app.repository.itinerary.ItineraryItemHotelInclusionRepository;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
import com.sss.app.repository.itinerary.ItineraryItemTransportDetailRepository;
import com.sss.app.repository.library.activity.ActivityRepository;
import com.sss.app.repository.library.transport.TransportRepository;
import com.sss.app.repository.quote.QuoteLineItemRepository;
import com.sss.app.repository.quote.QuoteRepository;
import com.sss.app.service.quote.QuoteComputationService;
import com.sss.app.service.quote.QuoteFingerprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * was never filled in. Tax comes from a real, org-configurable TaxProfile
 * (its rate_percent, or a per-quote override — see taxRatePercentOverride).
 * FX conversion uses a manually-entered, frozen rate rather than a live
 * provider (no FX rate API integration has been requested/authorized yet).
 * Base/storage currency is INR — a display currency other than INR requires
 * an FX rate snapshot.
 *
 * Each itinerary item's resolved price is also snapshotted into its own
 * QuoteLineItem row (see syncLineItems) — the Quote tab's day-wise,
 * per-item breakdown, each carrying its own optional discount on top of the
 * quote's single overall discount below.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuoteComputationServiceImpl implements QuoteComputationService {

    // Provider, not a direct field: ItineraryItemHelper itself depends on this service (circular).
    private final ObjectProvider<ItineraryItemHelper> itineraryItemHelperProvider;
    private final QuoteFingerprintService quoteFingerprintService;
    private final QuoteHelper quoteHelper;
    private final QuoteRepository quoteRepository;
    private final QuoteResponseAssembler quoteResponseAssembler;
    private final QuoteLineItemRepository quoteLineItemRepository;
    private final QuoteLineItemMapper quoteLineItemMapper;
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
        List<String> warnings = new ArrayList<>();
        List<QuoteLineItem> lineItems = syncLineItems(quote, warnings);

        BigDecimal subtotal = sumFinal(lineItems);
        PricingBreakdownDTO breakdown = buildBreakdown(lineItems);

        Totals totals = applyTaxAndTotals(quote, subtotal, request.getTaxProfileUid(), request.getTaxRatePercentOverride(),
                request.getTcsRatePercent(), request.getDiscountType(), request.getDiscountValue(),
                request.getDisplayCurrencyCode(), request.getFxRateSnapshot());
        quote.setCancellationChargesInr(breakdown.getCancellationInr());
        Quote saved = quoteRepository.save(quote);

        int paxCount = quote.getItinerary().getEscape().getTravellers() != null
                ? quote.getItinerary().getEscape().getTravellers().size()
                : 0;

        QuoteComputeResponseDTO response = new QuoteComputeResponseDTO();
        response.setQuote(quoteResponseAssembler.toResponse(saved));
        response.setPricingWarnings(warnings);
        response.setDisplayTotal(totals.displayTotal());
        response.setBreakdown(breakdown);
        response.setPaxCount(paxCount);
        response.setPerPaxInr(paxCount > 0 ? totals.total().divide(BigDecimal.valueOf(paxCount), 2, RoundingMode.HALF_UP) : null);
        return response;
    }

    @Override
    public QuoteLineItemsResponseDTO getLineItems(UUID quoteUid) {
        Quote quote = quoteHelper.getByUid(quoteUid);
        List<String> warnings = new ArrayList<>();
        List<QuoteLineItem> lineItems = syncLineItems(quote, warnings);
        Quote saved = recomputeFromLineItems(quote, lineItems);
        return toLineItemsResponse(saved, lineItems, warnings);
    }

    @Override
    public QuoteLineItemsResponseDTO markGenerated(UUID quoteUid) {
        Quote quote = quoteHelper.getByUid(quoteUid);
        List<String> warnings = new ArrayList<>();
        List<QuoteLineItem> lineItems = syncLineItems(quote, warnings);
        Quote saved = recomputeFromLineItems(quote, lineItems);
        saved.setGeneratedAt(java.time.LocalDateTime.now());
        saved.setGeneratedFingerprint(quoteFingerprintService.compute(saved));
        saved = quoteRepository.save(saved);
        return toLineItemsResponse(saved, lineItems, warnings);
    }

    @Override
    public QuoteLineItemsResponseDTO updateLineItemDiscount(UUID quoteUid, UUID lineItemUid, QuoteLineItemDiscountUpdateRequestDTO request) {
        Quote quote = quoteHelper.getByUid(quoteUid);
        QuoteLineItem lineItem = quoteLineItemRepository.findByUid(lineItemUid)
                .filter(li -> li.getQuote().getSeqp().equals(quote.getSeqp()))
                .orElseThrow(() -> new NotFoundException("Quote line item not found"));

        String discountType = request.getDiscountType() != null ? request.getDiscountType() : "none";
        lineItem.setDiscountType(discountType);
        lineItem.setDiscountValue(request.getDiscountValue());
        lineItem.setFinalAmountInr(applyDiscount(lineItem.getBaseAmountInr(), discountType, request.getDiscountValue()));
        quoteLineItemRepository.save(lineItem);

        List<QuoteLineItem> lineItems = quoteLineItemRepository.findAllByQuote_SeqpOrderByDayNumberAscSortOrderAsc(quote.getSeqp());
        Quote saved = recomputeFromLineItems(quote, lineItems);
        return toLineItemsResponse(saved, lineItems, List.of());
    }

    private QuoteLineItemsResponseDTO toLineItemsResponse(Quote quote, List<QuoteLineItem> lineItems, List<String> warnings) {
        QuoteLineItemsResponseDTO response = new QuoteLineItemsResponseDTO();
        response.setQuote(quoteResponseAssembler.toResponse(quote));
        response.setLineItems(quoteLineItemMapper.toResponseList(lineItems));
        response.setPricingWarnings(warnings);
        return response;
    }

    // Recomputes a quote's subtotal/tax/TCS/discount/total off its current
    // line items, reusing whatever tax/TCS/overall-discount/currency
    // settings the quote already has saved (i.e. "the same settings, just
    // against the new per-item numbers") — the path used whenever a line
    // item's own discount changes, as opposed to compute()'s explicit
    // "the caller is choosing new settings" path.
    private Quote recomputeFromLineItems(Quote quote, List<QuoteLineItem> lineItems) {
        BigDecimal subtotal = sumFinal(lineItems);
        PricingBreakdownDTO breakdown = buildBreakdown(lineItems);
        applyTaxAndTotals(quote, subtotal, quote.getTaxProfileId(), quote.getTaxRatePercentOverride(),
                quote.getTcsRatePercent(), quote.getDiscountType(), quote.getDiscountValue(),
                quote.getCurrencyCode(), quote.getFxRateSnapshot());
        quote.setCancellationChargesInr(breakdown.getCancellationInr());
        return quoteRepository.save(quote);
    }

    private record Totals(BigDecimal total, BigDecimal displayTotal) {
    }

    // Sets subtotal/tax/tcs/discount/total (+ currency/fx) directly on
    // `quote` (not saved here — every caller saves right after, once it's
    // also set whatever else it owns, e.g. cancellationChargesInr).
    private Totals applyTaxAndTotals(Quote quote, BigDecimal subtotal, UUID taxProfileUid, BigDecimal taxRateOverride,
                                      BigDecimal tcsRatePercent, String discountTypeIn, BigDecimal discountValue,
                                      String displayCurrencyCode, BigDecimal fxRateSnapshot) {
        BigDecimal taxAmount = BigDecimal.ZERO;
        UUID resolvedTaxProfileUid = null;
        BigDecimal resolvedOverride = null;
        if (taxProfileUid != null) {
            TaxProfile taxProfile = taxProfileHelper.getByUid(taxProfileUid);
            BigDecimal ratePercent = taxRateOverride != null ? taxRateOverride : taxProfile.getRatePercent();
            taxAmount = subtotal.multiply(ratePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            resolvedTaxProfileUid = taxProfile.getUid();
            resolvedOverride = taxRateOverride;
        }

        // TCS is levied on the customer-facing package price — i.e. subtotal
        // plus GST — not on the pre-tax subtotal alone, matching how
        // outbound-tour-package TCS is actually charged in practice.
        BigDecimal tcsAmount = tcsRatePercent != null
                ? subtotal.add(taxAmount).multiply(tcsRatePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String discountType = discountTypeIn != null ? discountTypeIn : "none";
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
        if (displayCurrencyCode != null && !"INR".equalsIgnoreCase(displayCurrencyCode)) {
            if (fxRateSnapshot == null) {
                throw new BadRequestException("fxRateSnapshot is required when displayCurrencyCode is not INR");
            }
            displayTotal = total.multiply(fxRateSnapshot).setScale(2, RoundingMode.HALF_UP);
        }

        quote.setSubtotalInr(subtotal.setScale(2, RoundingMode.HALF_UP));
        quote.setTaxProfileId(resolvedTaxProfileUid);
        quote.setTaxRatePercentOverride(resolvedOverride);
        quote.setTaxAmountInr(taxAmount);
        quote.setTcsRatePercent(tcsRatePercent);
        quote.setTcsAmountInr(tcsAmount);
        quote.setDiscountType(discountType);
        quote.setDiscountValue(discountValue);
        quote.setTotalInr(total.setScale(2, RoundingMode.HALF_UP));
        quote.setCurrencyCode(displayCurrencyCode);
        quote.setFxRateSnapshot(fxRateSnapshot);

        return new Totals(total, displayTotal);
    }

    private BigDecimal sumFinal(List<QuoteLineItem> lineItems) {
        return lineItems.stream().map(QuoteLineItem::getFinalAmountInr).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PricingBreakdownDTO buildBreakdown(List<QuoteLineItem> lineItems) {
        PricingBreakdownDTO breakdown = new PricingBreakdownDTO();
        for (QuoteLineItem li : lineItems) {
            if (Boolean.TRUE.equals(li.getIsCancellation())) {
                breakdown.setCancellationInr(breakdown.getCancellationInr().add(li.getFinalAmountInr()));
                continue;
            }
            switch (li.getItemType()) {
                case "hotel" -> breakdown.setHotelsInr(breakdown.getHotelsInr().add(li.getFinalAmountInr()));
                case "activity" -> breakdown.setActivitiesInr(breakdown.getActivitiesInr().add(li.getFinalAmountInr()));
                case "transport" -> breakdown.setTransportInr(breakdown.getTransportInr().add(li.getFinalAmountInr()));
                default -> breakdown.setOtherInr(breakdown.getOtherInr().add(li.getFinalAmountInr()));
            }
        }
        return breakdown;
    }

    private BigDecimal applyDiscount(BigDecimal base, String discountType, BigDecimal discountValue) {
        BigDecimal discountAmount = switch (discountType == null ? "none" : discountType) {
            case "percent" -> discountValue != null
                    ? base.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            case "flat" -> discountValue != null ? discountValue : BigDecimal.ZERO;
            case "none" -> BigDecimal.ZERO;
            default -> throw new BadRequestException("discountType must be one of: none, percent, flat");
        };
        BigDecimal result = base.subtract(discountAmount);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result.setScale(2, RoundingMode.HALF_UP);
    }

    // Rebuilds `quote`'s line items to match the itinerary's current items,
    // in day/sort order: an item already represented keeps its own
    // discount (only its label/day/order/base price/cancellation flag
    // refresh), a newly-priceable item gets a fresh no-discount row, and a
    // line item whose itinerary item is gone (or no longer resolves to a
    // price — e.g. its price field was cleared) is deleted. Returns the
    // synced rows in display order.
    private List<QuoteLineItem> syncLineItems(Quote quote, List<String> warnings) {
        List<ItineraryItem> items = itineraryItemRepository
                .findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(quote.getItinerary().getSeqp());

        Map<Long, QuoteLineItem> existingByItemSeqp = new HashMap<>();
        for (QuoteLineItem li : quoteLineItemRepository.findAllByQuote_SeqpOrderByDayNumberAscSortOrderAsc(quote.getSeqp())) {
            existingByItemSeqp.put(li.getItineraryItem().getSeqp(), li);
        }

        // Same library-name resolution the Itinerary tab shows (hotel/activity/transport names).
        Map<UUID, String> labels = itineraryItemHelperProvider.getObject().resolveLabels(items);
        List<QuoteLineItem> ordered = new ArrayList<>();
        int order = 0;
        for (ItineraryItem item : items) {
            ItemPriceResult result = resolvePrice(item, warnings);
            QuoteLineItem existing = existingByItemSeqp.remove(item.getSeqp());
            if (result == null) {
                // No longer priceable (or never was) — drop any stale row for it.
                if (existing != null) {
                    quoteLineItemRepository.delete(existing);
                }
                continue;
            }

            String resolved = labels.get(item.getUid());
            String label = resolved != null && !resolved.isBlank()
                    ? resolved
                    : item.getTitle() != null && !item.getTitle().isBlank()
                    ? item.getTitle()
                    : capitalize(item.getItemType());

            QuoteLineItem lineItem = existing != null ? existing : QuoteLineItem.builder()
                    .quote(quote)
                    .itineraryItem(item)
                    .discountType("none")
                    .build();
            lineItem.setOrgId(quote.getOrgId());
            lineItem.setDayNumber(item.getDayNumber());
            lineItem.setItemType(item.getItemType());
            lineItem.setLabel(label);
            lineItem.setSortOrder(order++);
            lineItem.setIsCancellation(result.isCancellation());
            lineItem.setBaseAmountInr(result.amount().setScale(2, RoundingMode.HALF_UP));
            lineItem.setFinalAmountInr(applyDiscount(lineItem.getBaseAmountInr(), lineItem.getDiscountType(), lineItem.getDiscountValue()));
            ordered.add(quoteLineItemRepository.save(lineItem));
        }

        // Whatever's left in the map belongs to items removed from the
        // itinerary entirely (cascade would eventually handle it too, but
        // doing it here keeps a stale row from lingering until that item's
        // own delete happens to be flushed).
        if (!existingByItemSeqp.isEmpty()) {
            quoteLineItemRepository.deleteAll(existingByItemSeqp.values());
        }

        return ordered;
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace('_', ' ');
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
                // Same rule as a dropped hotel/activity (see those branches
                // below/above): once Drop, this item is no longer priced as
                // an active booking — its only contribution is whatever
                // cancellation charge was actually levied (zero if none was
                // entered), regardless of the original transport price.
                // Transport has no dedicated detail-level status column (see
                // ItineraryItemTransportDetail) — it uses the same base
                // ItineraryItem.status/cancellationChargeInr fields Activity
                // already relies on for the same reason.
                if (BookingStatus.DROP.equals(item.getStatus())) {
                    BigDecimal charge = item.getCancellationChargeInr() != null
                            ? item.getCancellationChargeInr()
                            : BigDecimal.ZERO;
                    return new ItemPriceResult(charge, true);
                }
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
