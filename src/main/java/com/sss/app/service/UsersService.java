package com.sss.app.service;

import com.sss.app.dto.UserDto;

public interface UsersService {
    UserDto getUserByUid(String uid);
}
