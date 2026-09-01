package com.sss.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class QuoteAnalyticsDTO {
    private long totalQuotes;
    private long acceptedQuotes;
    private long rejectedQuotes;
    /** accepted / totalQuotes — never computed against draft/superseded. */
    private double acceptanceRatePercent;
    private BigDecimal averageQuoteValueInr;
    private BigDecimal totalQuoteValueInr;
    private List<StatusCountDTO> statusBreakdown;
}
