package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ItineraryItemCreateRequestDTO {

    @NotNull(message = "itineraryUid is required")
    private UUID itineraryUid;

    @NotNull(message = "dayNumber is required")
    private Integer dayNumber;

    @NotNull(message = "itemType is required")
    private String itemType; // hotel / activity / transport

    @NotNull(message = "referenceId is required")
    private UUID referenceId;

    private String notes;
}
