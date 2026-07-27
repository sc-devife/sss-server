package com.sss.app.dto.lead;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeadResponseDTO extends LeadDTO {
    private Long seqp;
    private Long assignedToUserId;
    private String assignmentReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
