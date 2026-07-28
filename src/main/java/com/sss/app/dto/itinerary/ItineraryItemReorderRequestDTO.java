package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ItineraryItemReorderRequestDTO {

    @NotNull(message = "itineraryUid is required")
    private UUID itineraryUid;

    // Item uids in their new display order — sort_order is reassigned from array position.
    @NotEmpty(message = "orderedItemUids is required")
    private List<UUID> orderedItemUids;
}
