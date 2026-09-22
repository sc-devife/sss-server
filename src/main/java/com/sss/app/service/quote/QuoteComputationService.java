package com.sss.app.service.quote;

import com.sss.app.dto.quote.QuoteComputeRequestDTO;
import com.sss.app.dto.quote.QuoteComputeResponseDTO;
import com.sss.app.dto.quote.QuoteLineItemDiscountUpdateRequestDTO;
import com.sss.app.dto.quote.QuoteLineItemsResponseDTO;

import java.util.UUID;

public interface QuoteComputationService {
    QuoteComputeResponseDTO compute(UUID quoteUid, QuoteComputeRequestDTO request);

    /** Syncs the quote's line items to the itinerary's current items (preserving each item's own discount) and returns them alongside the quote's current totals. */
    QuoteLineItemsResponseDTO getLineItems(UUID quoteUid);

    /** Updates one line item's discount, recomputes the quote's totals off the line items' new final amounts (using the quote's last-used tax/TCS/overall-discount settings), and returns both. */
    QuoteLineItemsResponseDTO updateLineItemDiscount(UUID quoteUid, UUID lineItemUid, QuoteLineItemDiscountUpdateRequestDTO request);
}
