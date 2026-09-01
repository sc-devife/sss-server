package com.sss.app.dto.quote;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QuoteComputeResponseDTO {
    private QuoteResponseDTO quote;
    private List<String> pricingWarnings; // e.g. items with no resolvable price, excluded from the subtotal
    private BigDecimal displayTotal; // totalInr converted via fxRateSnapshot; null if priced in INR only
}
