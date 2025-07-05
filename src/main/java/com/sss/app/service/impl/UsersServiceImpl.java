package com.sss.app.service.impl;

import com.sss.app.UsersHelper;
import com.sss.app.dto.UserDto;
import com.sss.app.entity.User;
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
    public UserDto getUserByUid(String uid) {
        User user = usersHelper.getUserByUid(uid);
        return userMapper.toDto(user);
    }
}
