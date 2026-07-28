package com.sss.app.service.impl;

import com.sss.app.dto.users.UserAssignmentSettingsUpdateRequestDto;
import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.entity.users.User;
import com.sss.app.helper.UsersHelper;
import com.sss.app.mapper.UserMapper;
import com.sss.app.service.UsersService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersServiceImpl implements UsersService {

    UsersHelper usersHelper;
    UserMapper userMapper;

    public UsersServiceImpl(UsersHelper usersHelper, UserMapper userMapper) {
        this.usersHelper = usersHelper;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponseDto getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userMapper.toUserResponseDto(user);
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
}
