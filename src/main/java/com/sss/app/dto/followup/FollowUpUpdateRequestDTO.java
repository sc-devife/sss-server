package com.sss.app.dto.followup;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

// Field-level edit — comment/actionable/due date-time/assignee. Status
// changes go through the separate /status endpoint (FollowUpStatusUpdateRequestDTO),
// same "lifecycle actions are separate from field edits" split Lead uses.
@Data
public class FollowUpUpdateRequestDTO {
    @NotBlank(message = "Comment is required")
    private String comment;

    private Boolean actionable;
    private LocalDateTime dueAt;

    // Blank/null defaults to the current user, same as create.
    private String assignedToUid;
}
