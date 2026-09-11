package com.sss.app.dto.followup;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

// Flattened for the /follow-ups table and the Lead/Escape "Tasks & Comments"
// section — denormalizes just the display fields (names/labels) rather than
// making the frontend chase separate Lead/Escape/User lookups per row.
@Data
public class FollowUpResponseDTO {
    private UUID uid;
    private String comment;
    private Boolean actionable;
    private LocalDateTime dueAt;
    private String status;

    private UUID leadUid;
    private String leadName;

    private UUID escapeUid;
    private String escapeTripCode;

    private String assignedToUid;
    private String assignedToName;

    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
