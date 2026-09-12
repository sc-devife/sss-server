package com.sss.app.dto.library.amenity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AmenityCreateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;
}
