package com.sss.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * One row of a Sales report. `revenue` is null for the Sales Persons report,
 * which has no Revenue column.
 */
@Data
@AllArgsConstructor
public class SalesReportRowDTO {
    private String label;
    private long leads;
    private long completed;
    private long hold;
    private long cancelled;
    private BigDecimal revenue;
}
