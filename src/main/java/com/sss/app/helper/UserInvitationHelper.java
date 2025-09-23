package com.sss.app.helper;

import com.sss.app.entity.users.invitations.UserInvitation;
import com.sss.app.repository.InvitationTokenRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class UserInvitationHelper {

    @PersistenceContext
    private EntityManager entityManager;

    private final InvitationTokenRepository invitationRepository;

    public UserInvitationHelper(InvitationTokenRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public UserInvitation inviteUser(String email) {

        System.out.println("Inviting user with email: " + email);
        Optional<UserInvitation> existingInvitation = invitationRepository.findByEmail(email);
        if (existingInvitation.isPresent()) {
            UserInvitation invitation = existingInvitation.get();
            if (invitation.is_used() || invitation.getExpires_set().isBefore(LocalDateTime.now()) || invitationRepository.findByEmail(email).isPresent()) {
                throw new RuntimeException("Existing invitation is either used or expired.");
            }
            return invitation; // Return existing valid invitation
        }
        //Check if the email is already registered
        if (invitationRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered: " + email);
        }
        // Create a new invitation
       UserInvitation userInvitation = new UserInvitation();
        userInvitation.setEmail(email);
        userInvitation.setExpires_set(LocalDateTime.now().plusDays(1)); // example: expires in 1 day
        userInvitation = invitationRepository.save(userInvitation);
        entityManager.refresh(userInvitation);
        System.out.println("getUid::::::::::::" + userInvitation);
        System.out.println("getUid::::::::::::" + userInvitation.getUid());
        String link = "http://localhost:8080/sss/newuser/" + userInvitation.getUid();
        sendInvitationEmail(email, link);
         return userInvitation;
    }

    public UserInvitation getInvitationDetails(String uid) {
        Optional<UserInvitation> invitationOpt = invitationRepository.findByUid(uid);

        if (invitationOpt.isEmpty() || invitationOpt.get().is_used() || invitationOpt.get().getExpires_set().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation not valid: " + uid);
        }
        if (invitationOpt.isPresent()) {
            return invitationOpt.get();
        } else {
            throw new RuntimeException("Invitation not found for UID: " + uid);
        }
    }
    @Autowired
    JavaMailSender mailSender = null;

    private void sendInvitationEmail(String email, String link) {
        System.out.println("email::" + email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("You're invited to join!");
        message.setText("Click the link to register: " + link);
        mailSender.send(message);
    }
}
