package com.sss.app.dto.quote;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Subtotal split by itinerary item category — computed in the SAME loop as
 * QuoteComputationServiceImpl's overall subtotal (same resolvePrice() calls,
 * same warnings), just grouped by item type instead of summed into one
 * number. Not persisted on Quote; recomputed fresh on every compute() call
 * so it always matches the itinerary's current items.
 */
@Data
public class PricingBreakdownDTO {
    private BigDecimal hotelsBase = BigDecimal.ZERO;
    private BigDecimal activitiesBase = BigDecimal.ZERO;
    private BigDecimal transportBase = BigDecimal.ZERO;
    private BigDecimal otherBase = BigDecimal.ZERO;

    // Cancellation charges from Dropped hotel bookings — kept separate from
    // hotelsBase so that bucket stays an accurate "active hotel cost" figure.
    private BigDecimal cancellationBase = BigDecimal.ZERO;
}
