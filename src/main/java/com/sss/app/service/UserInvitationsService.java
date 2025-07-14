package com.sss.app.service;

import com.sss.app.dto.UserDto;
import com.sss.app.dto.UserInvitationDto;
import com.sss.app.entity.UserInvitation;
import com.sss.app.repository.InvitationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface UserInvitationsService {
    UserInvitationDto inviteUser(String email);
}
