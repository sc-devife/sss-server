package com.sss.app.dto.users;

import com.sss.app.dto.userrolelinks.UserRoleLinkResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class UserResponseDto extends UserDto {
    private Long seqp;
    private String uid;
    private String userId;
    private String name;
    private List<UserRoleLinkResponseDto> roles;

    // Populated only on the self-service profile endpoints (getCurrentUser /
    // updateCurrentUser) — see UsersServiceImpl. Joined from the User's
    // existing orgId relationship, not duplicated into the users table.
    private String organizationName;
    private String organizationLogo;

    // Section 5 assignment-engine settings — see UserAssignmentSettingsUpdateRequestDto.
    private Boolean isSpecialist;
    private List<Long> specialistDestinations;
    private Integer maxConcurrentAssignments;
    private Boolean eligibleForPriorityLeads;
    private Boolean acceptingLeads;
    private Boolean blocked;
}
