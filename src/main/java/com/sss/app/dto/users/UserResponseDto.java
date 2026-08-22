package com.sss.app.dto.users;

import com.sss.app.dto.userrolelinks.UserRoleLinkResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
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
    private String organizationLogoShape;

    // Section 5 assignment-engine settings — see UserAssignmentSettingsUpdateRequestDto.
    private Boolean isSpecialist;
    private List<Long> specialistEscapePoints;
    private Integer maxConcurrentAssignments;
    private Boolean eligibleForPriorityLeads;
    private Boolean acceptingLeads;
    private Boolean blocked;

    // Resolved from UserSession.lastAccessed (keyed by email, refreshed on
    // every authenticated request — see JwtAuthenticationFilter) rather than
    // stored on the user record itself. Only populated on fetchAllUsers —
    // see UsersServiceImpl.
    private LocalDateTime lastActiveAt;

    // Resolved from User.invitedBy (the inviter's seqp) to a display name —
    // see UsersServiceImpl. Only populated on fetchAllUsers.
    private String invitedByName;
}
