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
    private BigDecimal subtotalInr;
    private UUID taxProfileId;
    private BigDecimal taxAmountInr;
    private BigDecimal tcsRatePercent;
    private BigDecimal tcsAmountInr;
    private BigDecimal totalInr;
    private String discountType;
    private BigDecimal discountValue;
    private UUID templateId;
    private LocalDate validUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
}
