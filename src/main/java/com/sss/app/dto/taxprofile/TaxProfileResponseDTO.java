package com.sss.app.dto.taxprofile;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TaxProfileResponseDTO {
    private UUID uid;
    private String name;
    private String displayName;
    private String description;
    private BigDecimal ratePercent;
    private String status;
}
