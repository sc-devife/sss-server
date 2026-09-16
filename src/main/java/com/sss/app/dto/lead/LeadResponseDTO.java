package com.sss.app.dto.lead;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LeadResponseDTO extends LeadDTO {
    private UUID uid;
    private LocalDate followUpDueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Read-only — deliberately not on the base LeadDTO shared with create/
    // update requests, so a client can never set assignment directly (only
    // LeadAssignmentService writes it). assignedToUserName is resolved via a
    // live UserRepository lookup, not stored — see
    // LeadServiceImpl.enrichAssignedToName, same "resolved, not stored"
    // pattern as EscapeResponseDTO's own pair of fields.
    private Long assignedToUserId;
    private String assignedToUserName;
    private String assignmentReason;
}
