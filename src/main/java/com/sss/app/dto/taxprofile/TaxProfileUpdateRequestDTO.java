package com.sss.app.dto.taxprofile;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaxProfileUpdateRequestDTO {
    private String name;
    private String displayName;
    private String description;
    private BigDecimal ratePercent;
    private String status; // active / inactive
}
