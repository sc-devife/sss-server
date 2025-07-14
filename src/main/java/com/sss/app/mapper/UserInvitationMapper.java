package com.sss.app.mapper;

import com.sss.app.dto.users.invitations.UserInvitationDto;
import com.sss.app.entity.users.invitations.UserInvitation;
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
