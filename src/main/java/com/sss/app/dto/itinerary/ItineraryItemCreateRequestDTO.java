package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class ItineraryItemCreateRequestDTO {

    @NotNull(message = "itineraryUid is required")
    private UUID itineraryUid;

    @NotNull(message = "dayNumber is required")
    private Integer dayNumber;

    @NotNull(message = "itemType is required")
    private String itemType; // transport/pickup_drop/hotel/activity/sightseeing/meal/free_time/other

    // Optional — see ItineraryItemHelper.validateReference: either this or
    // title must be supplied.
    private UUID referenceId;

    // Required if referenceId is absent (cross-field rule, enforced in the
    // helper rather than via a bean-validation annotation).
    private String title;

    private LocalTime startTime;

    private String notes;
}
