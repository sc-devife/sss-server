package com.sss.app.dto.lead;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LeadResponseDTO extends LeadDTO {
    private UUID uid;
    private Long assignedToUserId;
    // Resolved from assignedToUserId via a live UserRepository lookup (not a
    // stored column) — see EscapeServiceImpl.enrichAssignedToName. Only
    // populated on the Escape read paths today; mirrors the existing
    // client-side resolution pattern in LeadDetailModal.tsx.
    private String assignedToUserName;
    private String assignmentReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
