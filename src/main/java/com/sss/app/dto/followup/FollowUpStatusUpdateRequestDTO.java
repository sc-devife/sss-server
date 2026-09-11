package com.sss.app.dto.followup;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FollowUpStatusUpdateRequestDTO {
    @NotBlank(message = "Status is required")
    private String status; // Pending | Hold | Completed — validated against FollowUpStatus.ALL
}
