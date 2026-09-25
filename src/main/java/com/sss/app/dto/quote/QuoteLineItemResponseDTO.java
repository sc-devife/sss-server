package com.sss.app.dto.quote;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class QuoteLineItemResponseDTO {
    private UUID uid;
    private UUID itineraryItemUid;
    private Integer dayNumber;
    private String itemType;
    private String label;
    private boolean cancellation;
    private BigDecimal grossAmountBase;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal netAmountBase;
}
