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
}
