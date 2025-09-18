package com.sss.app.service;

import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.dto.users.UserUpdateRequestDto;

import java.util.List;

public interface UsersService {
    List<UserResponseDto> fetchAllUsers(Long companyId);

    UserResponseDto getUserByUid(String uid);

    UserResponseDto createUser(UserCreateRequestDto payload);

    UserResponseDto updateUser(String uid, UserUpdateRequestDto payload);

    UserResponseDto reassignRoles(String uid, List<String> roles);
}
