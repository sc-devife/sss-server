package com.sss.app.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Section 4: "Super Admin/Admin see org-wide metrics (leads in, conversion
 * rate, escapes in progress, revenue pipeline)." Only populated for callers
 * holding organizations.read — see DashboardServiceImpl.
 */
@Data
public class DashboardOrgMetricsDTO {
    private long leadsInLast30Days;
    private double conversionRatePercent;
    private long escapesInProgress;
    private BigDecimal revenuePipelineInr;

    // Trend-arrow comparisons — only for genuine period-flow metrics (a
    // count/sum accrued *within* a window). Point-in-time gauges like
    // escapesInProgress/revenuePipelineInr have no historical snapshot to
    // compare against, so they intentionally have no previous-period field.
    private long previousPeriodLeadsCount;
    private BigDecimal previousPeriodRevenueCollectedInr;

    private BigDecimal revenueCollectedInr;
    private long overduePaymentsCount;
    private BigDecimal overduePaymentsAmountInr;
    /** Sum of totalInr across accepted quotes — the org's booked revenue. */
    private BigDecimal totalRevenueInr;

    private List<StatusCountDTO> leadFunnel;
    private List<NameCountDTO> leadSourceBreakdown;
    private List<StatusCountDTO> escapePipeline;
    private List<NameCountDTO> topEscapePoints;
    private List<PaymentStatusBreakdownDTO> paymentBreakdown;
    private QuoteAnalyticsDTO quoteAnalytics;
}
