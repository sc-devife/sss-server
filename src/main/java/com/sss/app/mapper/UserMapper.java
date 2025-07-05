package com.sss.app.mapper;

import com.sss.app.dto.UserDto;
import com.sss.app.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(
                user.getSeqp(),
                user.getName(),
                user.getFirst_name(),
                user.getLast_name(),
                user.getEmail(),
                user.getContact_number());
    }
}
