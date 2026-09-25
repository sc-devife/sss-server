package com.sss.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** FX gain/loss on customer payments received in a non-base currency, per currency. */
@Data
@AllArgsConstructor
public class FxReportResponseDTO {

    @Data
    @AllArgsConstructor
    public static class Row {
        private String currency;
        private long payments;
        /** Total received, in that currency. */
        private BigDecimal receivedTotal;
        /** Total credited against milestones, in base currency. */
        private BigDecimal creditedBase;
        /** What the money was actually worth in base when it arrived. */
        private BigDecimal valueAtReceiptBase;
        /** valueAtReceiptBase - creditedBase: positive = gain, negative = loss. */
        private BigDecimal netFxBase;
    }

    private String baseCurrency;
    private List<Row> rows;
    private BigDecimal netFxBase;
}
