package com.sss.app.mapper;

import com.sss.app.dto.UserDto;
import com.sss.app.dto.UserInvitationDto;
import com.sss.app.entity.User;
import com.sss.app.entity.UserInvitation;
import org.springframework.stereotype.Component;

@Component
public class UserInvitationMapper {
    public UserInvitationDto toInvitationDto(UserInvitation userInvitation) {
        if (userInvitation == null) return null;
        return new UserInvitationDto(
                userInvitation.getSeqp(),
                userInvitation.getUid(),
                userInvitation.getEmail());
                //userInvitation.getExpires);
    }
}
