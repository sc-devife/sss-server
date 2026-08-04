package com.sss.app.service.impl;

import com.sss.app.dto.users.UserAssignmentSettingsUpdateRequestDto;
import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.entity.users.User;
import com.sss.app.helper.UsersHelper;
import com.sss.app.mapper.UserMapper;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.service.UsersService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersServiceImpl implements UsersService {

    UsersHelper usersHelper;
    UserMapper userMapper;
    OrganizationRepository organizationRepository;

    public UsersServiceImpl(UsersHelper usersHelper, UserMapper userMapper, OrganizationRepository organizationRepository) {
        this.usersHelper = usersHelper;
        this.userMapper = userMapper;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public UserResponseDto getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return toProfileResponseDto(user);
    }

    // Deliberately bypasses updateUser's users.write permission gate — this
    // only ever touches the caller's own record (uid comes from the security
    // principal, never the request), so it's not equivalent to the
    // admin-facing "edit any user" capability that permission guards.
    @Override
    public UserResponseDto updateCurrentUser(UserUpdateRequestDto payload) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User updated = usersHelper.updateUser(user.getUid(), payload);
        return toProfileResponseDto(updated);
    }

    // Joins the caller's own organization through the existing User.orgId
    // relationship — org details are never duplicated onto the users table,
    // just looked up and attached to the response for the self-service
    // profile endpoints (getCurrentUser / updateCurrentUser) only.
    private UserResponseDto toProfileResponseDto(User user) {
        UserResponseDto dto = userMapper.toUserResponseDto(user);
        if (user.getOrgId() != null) {
            organizationRepository.findById(user.getOrgId()).ifPresent(organization -> {
                String displayName = organization.getDisplayName();
                dto.setOrganizationName(
                        (displayName != null && !displayName.isBlank()) ? displayName : organization.getRegisteredName()
                );
                dto.setOrganizationLogo(organization.getLogoFile());
            });
        }
        return dto;
    }

    @Override
    public List<UserResponseDto> fetchAllUsers(Long companyId) {
        return userMapper.toUserResponseDtoList(usersHelper.fetchAllUsers(companyId));
    }

    @Override
    public UserResponseDto getUserByUid(String uid) {
        return userMapper.toUserResponseDto(usersHelper.getUserByUid(uid));
    }

    @Override
    public UserResponseDto createUser(UserCreateRequestDto dto) {
        return userMapper.toUserResponseDto(usersHelper.createUser(dto));
    }

    @Override
    public UserResponseDto updateUser(String uid, UserUpdateRequestDto payload) {
        return userMapper.toUserResponseDto(usersHelper.updateUser(uid, payload));
    }

    @Override
    public UserResponseDto updateAssignmentSettings(String uid, UserAssignmentSettingsUpdateRequestDto payload) {
        return userMapper.toUserResponseDto(usersHelper.updateAssignmentSettings(uid, payload));
    }

    @Override
    public UserResponseDto reassignRoles(String uid, List<String> roles) {
        return userMapper.toUserResponseDto(usersHelper.reassignRoles(uid, roles));
    }

    @Override
    public UserResponseDto setBlocked(String uid, boolean blocked) {
        return userMapper.toUserResponseDto(usersHelper.setBlocked(uid, blocked));
    }
}
