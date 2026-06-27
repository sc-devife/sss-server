package com.sss.app.dto.library.location;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationCreateRequestDTO {

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    private String country;

    @NotBlank(message = "Display name is required")
    private String displayName;
}
