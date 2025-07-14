package com.sss.app.service.impl;

import com.sss.app.Helper.UserInvitationHelper;
import com.sss.app.UsersHelper;
import com.sss.app.dto.UserDto;
import com.sss.app.dto.UserInvitationDto;
import com.sss.app.entity.User;
import com.sss.app.entity.UserInvitation;
import com.sss.app.mapper.UserInvitationMapper;
import com.sss.app.mapper.UserMapper;
import com.sss.app.repository.InvitationTokenRepository;
import com.sss.app.service.UserInvitationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

