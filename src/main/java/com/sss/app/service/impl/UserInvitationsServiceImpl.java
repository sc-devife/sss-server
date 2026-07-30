package com.sss.app.service.impl;

import com.sss.app.helper.UserInvitationHelper;
import com.sss.app.dto.users.invitations.UserInvitationDto;
import com.sss.app.entity.users.invitations.UserInvitation;
import com.sss.app.mapper.UserInvitationMapper;
import com.sss.app.service.UserInvitationsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInvitationsServiceImpl implements UserInvitationsService {

    UserInvitationHelper userInvitationHelper;
    UserInvitationMapper userInvitationMapper;

    public UserInvitationsServiceImpl(UserInvitationHelper userInvitationHelper, UserInvitationMapper userInvitationMapperMapper) {
        this.userInvitationHelper = userInvitationHelper;
        this.userInvitationMapper = userInvitationMapperMapper;
    }

    @Override
    public UserInvitationDto inviteUser(String email, List<String> roles) {
        UserInvitation userInvitation = userInvitationHelper.inviteUser(email, roles);
        return userInvitationMapper.toInvitationDto(userInvitation);
    }

    @Override
    public List<UserInvitationDto> listPendingInvitations() {
        return userInvitationMapper.toInvitationDtoList(userInvitationHelper.listPendingInvitations());
    }

    @Override
    public void cancelInvitation(Long invitationId) {
        userInvitationHelper.cancelInvitation(invitationId);
    }
}
