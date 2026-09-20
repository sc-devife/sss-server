package com.sss.app.dto.library.activity;

import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ActivityResponseDTO {

    private UUID uid;

    private String name;

    private EscapePointResponseDto escapePoint;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
