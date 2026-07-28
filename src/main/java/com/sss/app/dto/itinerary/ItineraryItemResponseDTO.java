package com.sss.app.dto.itinerary;

import lombok.Data;

import java.util.UUID;

@Data
public class ItineraryItemResponseDTO {
    private UUID uid;
    private UUID itineraryUid;
    private Integer dayNumber;
    private String itemType;
    private UUID referenceId;
    private String referenceLabel; // resolved at response-build time (hotel/activity/transport name)
    private String notes;
    private Integer sortOrder;
}
