package com.sss.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/** One PaymentMilestone.status bucket — billed vs collected within that status. */
@Data
@AllArgsConstructor
public class PaymentStatusBreakdownDTO {
    private String status;
    private long count;
    private BigDecimal totalUsd;
    private BigDecimal paidUsd;
}
