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

    // Optional — a blank/omitted name is auto-generated server-side from the
    // itinerary's name and the quote count for that itinerary.
    private String name;

    private String currencyCode;
    private BigDecimal fxRateSnapshot;
    private BigDecimal subtotalInr;
    private UUID taxProfileId;
    private BigDecimal taxAmountInr;
    private BigDecimal totalInr;
    private String discountType; // none / percent / flat
    private BigDecimal discountValue;
    private UUID templateId;
    private LocalDate validUntil;
}
