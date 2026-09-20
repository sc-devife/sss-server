package com.sss.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class SalesReportResponseDTO {
    /** sales-persons | escape-points | lead-sources */
    private String type;
    private LocalDate from;
    private LocalDate to;
    /** Whether the rows carry a Revenue figure (false for sales-persons). */
    private boolean hasRevenue;
    private List<SalesReportRowDTO> rows;
}
