package com.sss.app.dto.library.service;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ServiceResponseDTO {

    private UUID uid;

    private String name;

    private String description;

    private BigDecimal price;

    private Boolean isActive;

    // Null for global master-data services; set to the owning hotel's uid
    // for a service created via that hotel's "+ Add Services".
    private UUID hotelId;
}
