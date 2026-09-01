package com.sss.app.dto.library.service;

import lombok.Data;

@Data
public class ServiceUpdateRequestDTO {

    private String name;

    private String description;

    private Boolean isActive;
}
