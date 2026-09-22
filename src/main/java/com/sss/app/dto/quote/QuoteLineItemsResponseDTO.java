package com.sss.app.dto.quote;

import lombok.Data;

import java.util.List;

/** Line items plus the quote they belong to — returned after any sync/edit so the frontend's totals and per-item rows never fall out of sync. */
@Data
public class QuoteLineItemsResponseDTO {
    private QuoteResponseDTO quote;
    private List<QuoteLineItemResponseDTO> lineItems;
    private List<String> pricingWarnings;
}
