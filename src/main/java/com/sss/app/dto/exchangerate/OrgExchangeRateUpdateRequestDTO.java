package com.sss.app.dto.exchangerate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrgExchangeRateUpdateRequestDTO {
    /** "1 base currency = rate <to currency>". */
    @NotNull(message = "rate is required")
    @DecimalMin(value = "0.0000000001", message = "rate must be greater than 0")
    private BigDecimal rate;

    /** true = use this rate and never refresh it from the market; false = follow the market rate. */
    private Boolean manual;
}
