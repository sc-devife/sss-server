package com.sss.app.service.quote;

import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.quote.QuoteLineItem;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
import com.sss.app.repository.quote.QuoteLineItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * SHA-256 over everything that shapes a generated quotation: the quote's own
 * pricing settings and template, each priced line (label, base, discount,
 * final), and every itinerary item's identity + last-modified time (so an
 * edit that doesn't move a price still counts as a change).
 */
@Component
@RequiredArgsConstructor
public class QuoteFingerprintService {

    private final QuoteLineItemRepository quoteLineItemRepository;
    private final ItineraryItemRepository itineraryItemRepository;

    public String compute(Quote quote) {
        StringBuilder sb = new StringBuilder();
        sb.append(quote.getTemplateId()).append('|')
                .append(quote.getSubtotalInr()).append('|')
                .append(quote.getTaxProfileId()).append('|')
                .append(quote.getTaxRatePercentOverride()).append('|')
                .append(quote.getTaxAmountInr()).append('|')
                .append(quote.getTcsAmountInr()).append('|')
                .append(quote.getDiscountType()).append('|')
                .append(quote.getDiscountValue()).append('|')
                .append(quote.getTotalInr()).append('|')
                .append(quote.getCurrencyCode()).append('|')
                .append(quote.getFxRateSnapshot()).append('|')
                .append(quote.getValidUntil()).append('\n');

        List<QuoteLineItem> lineItems = quoteLineItemRepository
                .findAllByQuote_SeqpOrderByDayNumberAscSortOrderAsc(quote.getSeqp());
        for (QuoteLineItem li : lineItems) {
            sb.append(li.getItineraryItem().getUid()).append('|')
                    .append(li.getLabel()).append('|')
                    .append(li.getBaseAmountInr()).append('|')
                    .append(li.getDiscountType()).append('|')
                    .append(li.getDiscountValue()).append('|')
                    .append(li.getFinalAmountInr()).append('\n');
        }

        List<ItineraryItem> items = itineraryItemRepository
                .findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(quote.getItinerary().getSeqp());
        for (ItineraryItem item : items) {
            sb.append(item.getUid()).append('|').append(item.getUpdatedAt()).append('\n');
        }

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
