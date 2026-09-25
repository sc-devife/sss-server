package com.sss.app.dto.exchangerate;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row of the vendor's exchange-rate table: their base currency against another currency. */
@Data
@AllArgsConstructor
public class OrgExchangeRateRowDTO {
    private String currencyCode;
    private String currencyName;
    /** Market rate: 1 base = marketRate <currency>. Null when no market rate is available. */
    private BigDecimal marketRate;
    /** The vendor's own rate, if they ever set one (kept even when switched back to market). */
    private BigDecimal manualRate;
    private boolean manual;
    /** What actually applies now: the manual rate while manual, otherwise the market rate. */
    private BigDecimal effectiveRate;
    private LocalDate asOf;
}
