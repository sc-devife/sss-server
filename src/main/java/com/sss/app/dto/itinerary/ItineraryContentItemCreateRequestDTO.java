package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/** Escape-specific ad-hoc addition — no library item backing it. */
@Data
public class ItineraryContentItemCreateRequestDTO {

    @NotNull(message = "itineraryUid is required")
    private UUID itineraryUid;

    @NotBlank(message = "type is required")
    private String type; // INCLUSION / EXCLUSION / TERMS

    @NotBlank(message = "name is required")
    private String name;

    private String contentHtml;
}
