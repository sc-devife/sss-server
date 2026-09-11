package com.sss.app.dto.followup;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FollowUpCreateRequestDTO {
    // Exactly one of leadUid/escapeUid must be set — enforced in
    // FollowUpsHelper.createFollowUp, mirroring the DB's own CHECK constraint.
    private UUID leadUid;
    private UUID escapeUid;

    @NotBlank(message = "Comment is required")
    private String comment;

    private Boolean actionable;

    // Required when actionable=true; ignored (stored null) when false.
    private LocalDateTime dueAt;

    // User uid to assign to. Blank/null defaults to the current user
    // (FollowUpsHelper) — never resolved to a user outside the caller's org.
    private String assignedToUid;
}
