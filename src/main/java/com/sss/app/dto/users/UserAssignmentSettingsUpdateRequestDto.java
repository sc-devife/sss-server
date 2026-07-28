package com.sss.app.dto.users;

import lombok.Data;

import java.util.List;

/**
 * Section 5 / Lead Assigner tooling — per-agent assignment configuration,
 * kept separate from the general profile UserUpdateRequestDto since these
 * fields are an assignment-engine concern, not identity/contact info.
 */
@Data
public class UserAssignmentSettingsUpdateRequestDto {
    private Boolean isSpecialist;
    private List<Long> specialistDestinations;
    private Integer maxConcurrentAssignments;
    private Boolean eligibleForPriorityLeads;
    private Boolean acceptingLeads;
}
