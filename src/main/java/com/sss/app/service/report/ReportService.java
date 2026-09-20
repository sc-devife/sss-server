package com.sss.app.service.report;

import com.sss.app.dto.report.SalesReportResponseDTO;

import java.time.LocalDate;

public interface ReportService {

    /** type: sales-persons | escape-points | lead-sources; from/to inclusive (null = unbounded). */
    SalesReportResponseDTO getSalesReport(String type, LocalDate from, LocalDate to);

    /** The same report as CSV bytes (UTF-8 with BOM, so Excel reads it correctly). */
    byte[] exportSalesReportCsv(String type, LocalDate from, LocalDate to);
}
