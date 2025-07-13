package com.sss.app.dto.roles;

import lombok.Data;

@Data
public class RoleResponseDto {
    private Long seqp;

    private String uid;

    private String name;

    private String label;

    private Boolean is_active;

    private Boolean is_archived;
}
