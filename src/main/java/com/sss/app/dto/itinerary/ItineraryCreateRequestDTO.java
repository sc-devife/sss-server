package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItineraryCreateRequestDTO {

    @NotNull(message = "tripId is required")
    private Long tripId;

    @NotBlank(message = "Name is required")
    private String name;
}
