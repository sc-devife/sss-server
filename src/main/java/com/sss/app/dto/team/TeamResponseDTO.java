package com.sss.app.dto.team;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TeamResponseDTO {

    private UUID uid;

    private Long orgId;

    private String name;

    private String description;

    private String status;

    private List<Long> specializedEscapePoints;

    private Long teamLeadUserId;

    // Resolved from teamLeadUserId — see TeamServiceImpl.
    private String teamLeadUserName;

    private Integer maxConcurrentAssignments;

    // Resolved from UserTeamLinkRepository — see TeamServiceImpl.
    private List<TeamMemberSummaryDto> members;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
