package com.sss.app.dto.lead;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeadAssignRequestDTO {
    @NotNull(message = "userId is required")
    private Long userId;
    private String reason;
}
