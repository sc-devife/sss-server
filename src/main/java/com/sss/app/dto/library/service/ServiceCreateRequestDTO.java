package com.sss.app.dto.library.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ServiceCreateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private BigDecimal price;

    // Set only when created via a specific hotel's "+ Add Services" — scopes
    // the new service to that hotel instead of the global master-data list.
    // Omitted (null) for a create from the main Services module.
    private UUID hotelId;
}
