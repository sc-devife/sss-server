package com.sss.app.dto.escape;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** What reducing an escape to a shorter length would delete (nothing when the removed days are empty). */
@Data
@AllArgsConstructor
public class DayReductionImpactDTO {
    /** Removed days that actually hold planned items, ascending. */
    private List<Integer> daysWithItems;
    /** Itinerary items (across all of the escape's itineraries) that would be deleted. */
    private int itemCount;
    /** Quote lines that would go with them. */
    private int quoteLineCount;
}
