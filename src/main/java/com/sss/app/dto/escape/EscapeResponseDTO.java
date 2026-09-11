package com.sss.app.dto.escape;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EscapeResponseDTO extends EscapeDTO {
    private UUID uid;
    private Long assignedToUserId;
    // Resolved from assignedToUserId via a live UserRepository lookup (not a
    // stored column) — see EscapeServiceImpl.enrichAssignedToName.
    private String assignedToUserName;
    private String assignmentReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Private, team-only — never rendered in the Quotation (see
    // QuotationDataService, which deliberately never reads this field).
    private String internalComments;
    // Client-facing rich text, rendered in the generated Quotation.
    private String remarkForLead;
}
