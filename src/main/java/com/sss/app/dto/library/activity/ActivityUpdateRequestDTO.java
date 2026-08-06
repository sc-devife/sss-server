package com.sss.app.dto.library.activity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ActivityUpdateRequestDTO {

    private String name;

    private String escapePointId;

    private String categoryCode;

    private Integer durationMinutes;

    private String description;

    private List<String> images;

    private BigDecimal basePrice;

    private String status;
}
