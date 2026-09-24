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
    /** Records that the quotation was just generated: re-syncs the line items, then stores the time and a fingerprint of what it was generated from. */
    QuoteLineItemsResponseDTO markGenerated(UUID quoteUid);

    QuoteLineItemsResponseDTO updateLineItemDiscount(UUID quoteUid, UUID lineItemUid, QuoteLineItemDiscountUpdateRequestDTO request);
}
