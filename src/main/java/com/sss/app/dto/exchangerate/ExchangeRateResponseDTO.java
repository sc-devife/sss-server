package com.sss.app.dto.exchangerate;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The rate that applies to a pair for the caller's vendor: "1 from = rate to". */
@Data
@AllArgsConstructor
public class ExchangeRateResponseDTO {
    private String from;
    private String to;
    private BigDecimal rate;
    /** "market", "manual" or "same" (from == to). */
    private String source;
    private boolean manual;
    /** Date of the market rate (also shown next to manual rates for reference). */
    private LocalDate asOf;
    /** The market rate for the pair, for comparison with a manual one. */
    private BigDecimal marketRate;
}
