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
    private BigDecimal hotelsInr = BigDecimal.ZERO;
    private BigDecimal activitiesInr = BigDecimal.ZERO;
    private BigDecimal transportInr = BigDecimal.ZERO;
    private BigDecimal otherInr = BigDecimal.ZERO;
}
