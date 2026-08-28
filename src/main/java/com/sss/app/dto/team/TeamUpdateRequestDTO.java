package com.sss.app.dto.team;

import lombok.Data;

import java.util.List;

@Data
public class TeamUpdateRequestDTO {

    private String name;

    private String description;

    private String status;

    private List<Long> specializedEscapePoints;

    private Long teamLeadUserId;

    private Integer maxConcurrentAssignments;
}
