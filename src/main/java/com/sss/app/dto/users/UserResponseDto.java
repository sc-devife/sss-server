package com.sss.app.dto.users;

import com.sss.app.dto.userrolelinks.UserRoleLinkResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class UserResponseDto extends UserDto {
    private Long seqp;
    private String uid;
    private String name;
    private String email;
    private List<UserRoleLinkResponseDto> roles;
}
