package com.sss.app.dto.quote;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class QuoteUpdateRequestDTO {
    private String name;
    private String currencyCode;
    private BigDecimal fxRateSnapshot;
    private BigDecimal subtotalBase;
    private UUID taxProfileId;
    private BigDecimal taxAmountBase;
    private BigDecimal totalBase;
    private String discountType;
    private BigDecimal discountValue;
    private UUID templateId;
    private LocalDate validUntil;
    private String status; // draft / sent / accepted / rejected / superseded
}
