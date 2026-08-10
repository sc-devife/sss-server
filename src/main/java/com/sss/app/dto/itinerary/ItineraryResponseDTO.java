package com.sss.app.dto.itinerary;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ItineraryResponseDTO {
    private UUID uid;
    private UUID escapeUid;
    private String name;
    private String status;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
