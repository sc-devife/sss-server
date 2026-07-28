package com.sss.app.dto.quote;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class QuoteCreateRequestDTO {

    @NotNull(message = "itineraryUid is required")
    private UUID itineraryUid;

    private String currencyCode;
    private BigDecimal fxRateSnapshot;
    private BigDecimal subtotalUsd;
    private UUID taxProfileId;
    private BigDecimal taxAmountUsd;
    private BigDecimal totalUsd;
    private String discountType; // none / percent / flat
    private BigDecimal discountValue;
    private UUID templateId;
    private LocalDate validUntil;
}
