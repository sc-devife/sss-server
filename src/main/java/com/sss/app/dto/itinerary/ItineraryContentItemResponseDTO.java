package com.sss.app.dto.itinerary;

import lombok.Data;

import java.util.UUID;

@Data
public class ItineraryContentItemResponseDTO {
    private UUID uid;
    private UUID itineraryUid;
    private String type;
    private Long sourceItemId;
    private String name;
    private String contentHtml;
    private Integer sortOrder;
}
