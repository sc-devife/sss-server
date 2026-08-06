package com.sss.app.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

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
    private BigDecimal revenuePipelineUsd;
}
