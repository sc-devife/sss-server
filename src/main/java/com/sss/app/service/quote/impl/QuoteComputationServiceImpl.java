package com.sss.app.service.quote.impl;

import com.sss.app.dto.quote.QuoteComputeRequestDTO;
import com.sss.app.dto.quote.QuoteComputeResponseDTO;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.taxprofile.TaxProfile;
import com.sss.app.exception.BadRequestException;
import com.sss.app.helper.quote.QuoteHelper;
import com.sss.app.helper.taxprofile.TaxProfileHelper;
import com.sss.app.mapper.quote.QuoteMapper;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
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
 * Section 6 Quotation Engine — deliberately v1-scoped: sums pricing from
 * itinerary items that resolve to a priced library item (Activity/Transport
 * have base_price; Hotel doesn't have a price field yet, so hotel line
 * items are reported as excluded rather than silently treated as free).
 * Tax comes from a real, org-configurable TaxProfile. FX conversion uses a
 * manually-entered, frozen rate rather than a live provider (no FX rate API
 * integration has been requested/authorized yet). Base/storage currency is
 * INR — a display currency other than INR requires an FX rate snapshot.
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

    @Override
    public QuoteComputeResponseDTO compute(UUID quoteUid, QuoteComputeRequestDTO request) {
        Quote quote = quoteHelper.getByUid(quoteUid);

        List<ItineraryItem> items = itineraryItemRepository
                .findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(quote.getItinerary().getSeqp());

        List<String> warnings = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItineraryItem item : items) {
            BigDecimal price = resolvePrice(item, warnings);
            if (price != null) {
                subtotal = subtotal.add(price);
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
        return response;
    }

    private BigDecimal resolvePrice(ItineraryItem item, List<String> warnings) {
        switch (item.getItemType()) {
            case "activity" -> {
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
                warnings.add("Hotel on day " + item.getDayNumber() + " has no per-night pricing yet (not built) — excluded");
                return null;
            }
            default -> {
                return null;
            }
        }
    }
}
