package com.sss.app.dto.quote;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class QuoteResponseDTO {
    private UUID uid;
    private UUID itineraryUid;
    private String quoteCode;
    private String name;
    private Integer version;
    private String status;
    private String currencyCode;
    private BigDecimal fxRateSnapshot;
    private Boolean fxRateCustom;
    private String fxRateSource; // market / vendor / custom
    private java.time.LocalDate fxRateAsOf;
    private BigDecimal subtotalBase;
    private UUID taxProfileId;
    private BigDecimal taxRatePercentOverride;
    private BigDecimal taxAmountBase;
    private BigDecimal tcsRatePercent;
    private BigDecimal tcsAmountBase;
    private BigDecimal totalBase;
    private BigDecimal cancellationChargesBase;
    private String discountType;
    private BigDecimal discountValue;
    private UUID templateId;
    private LocalDate validUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
    private LocalDateTime generatedAt;
    // True when the quote was generated but has changed since (set by QuoteResponseAssembler).
    private Boolean changedSinceGenerated;
}
