package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ItineraryCreateRequestDTO {

    @NotNull(message = "escapeUid is required")
    private UUID escapeUid;

    @NotBlank(message = "Name is required")
    private String name;
}
