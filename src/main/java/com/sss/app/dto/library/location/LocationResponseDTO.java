package com.sss.app.dto.library.location;

import lombok.Data;

import java.util.UUID;

@Data
public class LocationResponseDTO {

    private UUID uid;

    private String city;

    private String state;

    private String country;

    private String displayName;

    private Boolean isActive;
}
