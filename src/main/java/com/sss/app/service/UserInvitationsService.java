package com.sss.app.service;

import com.sss.app.dto.users.invitations.UserInvitationDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserInvitationsService {
    UserInvitationDto inviteUser(String email, List<String> roles);

    List<UserInvitationDto> listPendingInvitations();

    void cancelInvitation(Long invitationId);
}
