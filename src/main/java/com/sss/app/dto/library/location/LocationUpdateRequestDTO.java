package com.sss.app.dto.library.location;

import lombok.Data;

@Data
public class LocationUpdateRequestDTO {

    private String city;

    private String state;

    private String country;

    private String displayName;

    private Boolean isActive;
}
