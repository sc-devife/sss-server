package com.sss.app.dto.library.service;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceUpdateRequestDTO {

    private String name;

    private String description;

    private BigDecimal price;

    private Boolean isActive;
}
