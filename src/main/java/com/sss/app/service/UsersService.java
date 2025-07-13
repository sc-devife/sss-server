package com.sss.app.service;

import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;

public interface UsersService {
    UserResponseDto getUserByUid(String uid);
    UserResponseDto createUser(UserCreateRequestDto dto);
}
