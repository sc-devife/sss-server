package com.sss.app.dto.quote;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QuoteComputeResponseDTO {
    private QuoteResponseDTO quote;
    private List<String> pricingWarnings; // e.g. items with no resolvable price, excluded from the subtotal
    private BigDecimal displayTotal; // totalInr converted via fxRateSnapshot; null if priced in INR only
    private PricingBreakdownDTO breakdown; // subtotal split by item category — same resolvePrice() sums as subtotalInr, just grouped
    private Integer paxCount; // escape's traveller count at compute time — not persisted, purely for display
    private BigDecimal perPaxInr; // totalInr / paxCount; null if paxCount is 0
}
