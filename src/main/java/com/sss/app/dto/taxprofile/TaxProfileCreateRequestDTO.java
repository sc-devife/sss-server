package com.sss.app.dto.taxprofile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaxProfileCreateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Display name is required")
    private String displayName;

    private String description;

    @NotNull(message = "ratePercent is required")
    private BigDecimal ratePercent;
}
