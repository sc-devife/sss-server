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
    private BigDecimal subtotalInr;
    private UUID taxProfileId;
    private BigDecimal taxAmountInr;
    private BigDecimal totalInr;
    private String discountType;
    private BigDecimal discountValue;
    private UUID templateId;
    private LocalDate validUntil;
    private String status; // draft / sent / accepted / rejected / superseded
}
