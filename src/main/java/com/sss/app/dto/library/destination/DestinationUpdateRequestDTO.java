package com.sss.app.dto.library.destination;

import lombok.Data;

@Data
public class DestinationUpdateRequestDTO {

    private String name;

    private String description;

    private Boolean isActive;
}
