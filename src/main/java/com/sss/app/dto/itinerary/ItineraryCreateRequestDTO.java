package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ItineraryCreateRequestDTO {

    @NotNull(message = "escapeUid is required")
    private UUID escapeUid;

    // Optional — a blank/omitted name is auto-generated server-side from the
    // escape's lead name, itinerary count, and trip length.
    private String name;
}
