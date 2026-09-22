package com.sss.app.dto.quote;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuoteLineItemDiscountUpdateRequestDTO {
    // none / percent / flat — required (not IGNORE-on-null like most update
    // DTOs) since "clear this item's discount" is a real, common edit.
    private String discountType;
    private BigDecimal discountValue;
}
