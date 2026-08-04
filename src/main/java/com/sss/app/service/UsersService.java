package com.sss.app.service;

import com.sss.app.dto.users.UserAssignmentSettingsUpdateRequestDto;
import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.dto.users.UserUpdateRequestDto;

import java.util.List;

public interface UsersService {
    UserResponseDto getCurrentUser();

    UserResponseDto updateCurrentUser(UserUpdateRequestDto payload);

    List<UserResponseDto> fetchAllUsers(Long companyId);

    UserResponseDto getUserByUid(String uid);

    UserResponseDto createUser(UserCreateRequestDto payload);

    UserResponseDto updateUser(String uid, UserUpdateRequestDto payload);

    UserResponseDto updateAssignmentSettings(String uid, UserAssignmentSettingsUpdateRequestDto payload);

    UserResponseDto reassignRoles(String uid, List<String> roles);

    UserResponseDto setBlocked(String uid, boolean blocked);
}
