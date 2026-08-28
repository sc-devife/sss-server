package com.sss.app.dto.team;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class TeamCreateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private String status;

    private List<Long> specializedEscapePoints;

    private Long teamLeadUserId;

    private Integer maxConcurrentAssignments;
}
