package com.sss.app.dto.library.destination;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DestinationCreateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
