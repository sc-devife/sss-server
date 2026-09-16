package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

// Moves an entire day's items to a different day POSITION — the date stays
// attached to the position (it's always derived from escape.startDate +
// dayNumber, never stored per item), only which items occupy that position
// changes. See ItineraryItemHelper.reorderDays for the shift semantics.
@Data
public class ItineraryItemReorderDaysRequestDTO {

    @NotNull(message = "itineraryUid is required")
    private UUID itineraryUid;

    @NotNull(message = "fromDayNumber is required")
    private Integer fromDayNumber;

    @NotNull(message = "toDayNumber is required")
    private Integer toDayNumber;
}
