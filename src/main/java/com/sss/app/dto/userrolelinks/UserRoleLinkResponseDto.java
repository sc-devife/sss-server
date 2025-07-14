package com.sss.app.dto.userrolelinks;

import com.sss.app.dto.roles.RoleResponseDto;
import lombok.Data;

@Data
public class UserRoleLinkResponseDto {
    private Long seqp;

    private String uid;

    private Boolean is_active;

    private Boolean is_archived;

    private RoleResponseDto role;
}
