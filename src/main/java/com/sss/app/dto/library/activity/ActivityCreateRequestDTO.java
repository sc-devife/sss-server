package com.sss.app.dto.library.activity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ActivityCreateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String escapePointId;

    private String categoryCode;

    private Integer durationMinutes;

    private String description;

    private List<String> images;

    private BigDecimal basePrice;

    private String status;

    private String notes;

    private String email;

    private String contactNumber;

    private String rulesAndPolicies;

    private String accountHolderName;

    private String bankName;

    private String branchName;

    private String accountType;

    private String accountNumber;

    private String ifsc;

    private String upiId;
}
