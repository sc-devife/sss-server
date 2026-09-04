package com.sss.app.dto.quote;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class QuoteComputeRequestDTO {
    private UUID taxProfileUid; // null = no tax applied
    private BigDecimal tcsRatePercent; // null/zero = no TCS applied; computed on (subtotal + GST)
    private String discountType; // none / percent / flat
    private BigDecimal discountValue;

    // Section 6: "freeze the rate used" — no live FX provider is wired in
    // yet (that needs a rate-provider integration decision), so a staff
    // member enters the rate manually at compute time and it's frozen from
    // there. Leave both null to price in INR only.
    private String displayCurrencyCode;
    private BigDecimal fxRateSnapshot;
}
