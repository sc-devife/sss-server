package com.sss.app.service;

import com.sss.app.dto.users.invitations.UserInvitationDto;
import org.springframework.stereotype.Service;

@Service
public interface UserInvitationsService {
    UserInvitationDto inviteUser(String email);
}
