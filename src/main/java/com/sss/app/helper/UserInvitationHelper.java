package com.sss.app.helper;

import com.sss.app.entity.users.invitations.UserInvitation;
import com.sss.app.repository.InvitationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class UserInvitationHelper {

    private final InvitationTokenRepository invitationRepository;

    public UserInvitationHelper(InvitationTokenRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    public UserInvitation inviteUser(String email) {
        //Check if the email is already registered
        //if (invitationRepository.findByEmail(Email).isPresent()) {
        //    throw new RuntimeException("Email already registered: " + Email);
        //}

        // Check if an invitation already exists for the given email
/*        if (invitationRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Invitation already exists for Email: " + email);
        }*/
        // Create a new UserInvitation object
        UserInvitation userInvitation = new UserInvitation();
        userInvitation.setEmail(email);
        // Save the UserInvitation object to the repository
        invitationRepository.save(userInvitation);
        String link = "http://localhost:8080/sss/newuser/redirect?token=" + userInvitation.getUid();
        sendInvitationEmail(email, link);
         return userInvitation;
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
