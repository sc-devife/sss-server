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
    private String name;
    private List<UserRoleLinkResponseDto> roles;

    // Section 5 assignment-engine settings — see UserAssignmentSettingsUpdateRequestDto.
    private Boolean isSpecialist;
    private List<Long> specialistDestinations;
    private Integer maxConcurrentAssignments;
    private Boolean eligibleForPriorityLeads;
    private Boolean acceptingLeads;
    private Boolean blocked;
}
