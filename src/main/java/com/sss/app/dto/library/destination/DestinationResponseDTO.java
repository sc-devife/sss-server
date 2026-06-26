package com.sss.app.dto.library.destination;

import lombok.Data;

import java.util.UUID;

@Data
public class DestinationResponseDTO {

    private UUID uid;

    private String name;

    private String description;

    private Boolean isActive;
}
