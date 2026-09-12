package com.sss.app.dto.library.amenity;

import lombok.Data;

import java.util.UUID;

@Data
public class AmenityResponseDTO {

    private UUID uid;

    private String name;

    private Boolean isActive;
}
