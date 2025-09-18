package com.sss.app.mapper;

import com.sss.app.dto.roles.RoleResponseDto;
import com.sss.app.dto.userrolelinks.UserRoleLinkResponseDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.entity.roles.Role;
import com.sss.app.entity.userrolelinks.UserRoleLink;
import com.sss.app.entity.users.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    private static UserRoleLinkResponseDto getUserRoleLinkDto(UserRoleLink link) {
        Role role = link.getRole();
        RoleResponseDto roleResponseDto = new RoleResponseDto();
        roleResponseDto.setSeqp(role.getSeqp());
        roleResponseDto.setUid(role.getUid());
        roleResponseDto.setName(role.getName());
        roleResponseDto.setLabel(role.getLabel());
        roleResponseDto.setIs_active(role.getIs_active());
        roleResponseDto.setIs_archived(role.getIs_archived());

        UserRoleLinkResponseDto urDto = new UserRoleLinkResponseDto();
        urDto.setSeqp(link.getSeqp());
        urDto.setUid(link.getUid());
        urDto.setIs_active(link.getIs_active());
        urDto.setIs_archived(link.getIs_archived());
        urDto.setRole(roleResponseDto);
        return urDto;
    }

    public UserResponseDto toUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setSeqp(user.getSeqp());
        dto.setUid(user.getUid());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setFirst_name(user.getFirst_name());
        dto.setLast_name(user.getLast_name());
        dto.setContact_number(user.getContact_number());

        List<UserRoleLinkResponseDto> roleDTOs = new ArrayList<>();
        for (UserRoleLink link : user.getRoles()) {
            UserRoleLinkResponseDto urDto = getUserRoleLinkDto(link);
            roleDTOs.add(urDto);
        }

        dto.setRoles(roleDTOs);
        return dto;
    }

    public List<UserResponseDto> toUserResponseDtoList(List<User> users) {
        return users.stream()
                .map(this::toUserResponseDto)
                .collect(Collectors.toList());
    }
}
