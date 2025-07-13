package com.sss.app.service.impl;

import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.helper.UsersHelper;
import com.sss.app.mapper.UserMapper;
import com.sss.app.service.UsersService;
import org.springframework.stereotype.Service;

@Service
public class UsersServiceImpl implements UsersService {

    UsersHelper usersHelper;
    UserMapper userMapper;

    public UsersServiceImpl(UsersHelper usersHelper, UserMapper userMapper) {
        this.usersHelper = usersHelper;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponseDto getUserByUid(String uid) {
        return userMapper.toUserResponseDto(usersHelper.getUserByUid(uid));
    }

    @Override
    public UserResponseDto createUser(UserCreateRequestDto dto) {
        return userMapper.toUserResponseDto(usersHelper.createUser(dto));
    }


}
