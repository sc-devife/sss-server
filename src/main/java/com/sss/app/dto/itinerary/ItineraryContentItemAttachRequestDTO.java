package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/** Attach an existing library item (InclusionExclusion) to an itinerary — content is copied in. */
@Data
public class ItineraryContentItemAttachRequestDTO {

    @NotNull(message = "itineraryUid is required")
    private UUID itineraryUid;

    @NotNull(message = "sourceItemUid is required")
    private String sourceItemUid;
}
