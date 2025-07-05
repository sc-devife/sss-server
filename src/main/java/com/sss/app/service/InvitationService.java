package com.sss.app.service;

import com.sss.app.entity.InvitationToken;
import com.sss.app.repository.InvitationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InvitationService {
    @Autowired
    private InvitationTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    public void inviteUser(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusDays(7);

        InvitationToken invitation = new InvitationToken();
        invitation.setEmail(email);
        invitation.setToken(token);
        invitation.setExpiryDate(expiry);
        //invitation.setExpiryDate(LocalDateTime.now().plusMinutes(1));
        invitation.setUsed(false);

        tokenRepository.save(invitation);

        String link = "http://localhost:8080/sss/test/register?token=" + token;
        sendInvitationEmail(email, link);
    }

    private void sendInvitationEmail(String email, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("You're invited to join!");
        message.setText("Click the link to register: " + link);
        mailSender.send(message);
    }
}
