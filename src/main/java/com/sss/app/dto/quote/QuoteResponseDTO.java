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
    private Integer version;
    private String status;
    private String currencyCode;
    private BigDecimal fxRateSnapshot;
    private BigDecimal subtotalUsd;
    private UUID taxProfileId;
    private BigDecimal taxAmountUsd;
    private BigDecimal totalUsd;
    private String discountType;
    private BigDecimal discountValue;
    private UUID templateId;
    private LocalDate validUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
