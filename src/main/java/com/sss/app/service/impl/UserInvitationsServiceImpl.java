package com.sss.app.service.impl;

import com.sss.app.Helper.UserInvitationHelper;
import com.sss.app.dto.users.invitations.UserInvitationDto;
import com.sss.app.entity.users.invitations.UserInvitation;
import com.sss.app.mapper.UserInvitationMapper;
import com.sss.app.service.UserInvitationsService;
import org.springframework.stereotype.Service;

@Service
public class UserInvitationsServiceImpl implements UserInvitationsService {

    UserInvitationHelper userInvitationHelper;
    UserInvitationMapper userInvitationMapper;

    public UserInvitationsServiceImpl(UserInvitationHelper userInvitationHelper, UserInvitationMapper userInvitationMapperMapper) {
        this.userInvitationHelper = userInvitationHelper;
        this.userInvitationMapper = userInvitationMapperMapper;
    }

    @Override
    public UserInvitationDto inviteUser(String Email) {
        UserInvitation userInvitation = userInvitationHelper.inviteUser(Email);
        return userInvitationMapper.toInvitationDto(userInvitation);
    }
}

