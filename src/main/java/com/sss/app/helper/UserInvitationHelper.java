package com.sss.app.helper;

import com.sss.app.entity.users.User;
import com.sss.app.entity.users.invitations.UserInvitation;
import com.sss.app.exception.ConflictException;
import com.sss.app.repository.InvitationTokenRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@Component
public class UserInvitationHelper {

    private static final long INVITATION_VALIDITY_DAYS = 7;

    private final InvitationTokenRepository invitationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public UserInvitationHelper(InvitationTokenRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public UserInvitation inviteUser(String email) {
        invitationRepository.findByEmail(email)
                .filter(existing -> !existing.is_used() && existing.getExpires_set().isAfter(LocalDateTime.now()))
                .ifPresent(existing -> {
                    throw new ConflictException("An active invitation already exists for " + email);
                });

        User inviter = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserInvitation userInvitation = new UserInvitation();
        userInvitation.setEmail(email);
        userInvitation.setOrgId(inviter.getOrgId());
        userInvitation.setInvitedBy(inviter.getSeqp());
        userInvitation.setExpires_set(LocalDateTime.now().plusDays(INVITATION_VALIDITY_DAYS));
        userInvitation.set_used(false);
        userInvitation.set_archived(false);

        userInvitation = invitationRepository.save(userInvitation);
        entityManager.flush();
        entityManager.refresh(userInvitation);

        String link = frontendUrl + "/signup?token=" + userInvitation.getUid() + "&email=" + email;
        sendInvitationEmail(email, link);
        return userInvitation;
    }

    @Autowired
    JavaMailSender mailSender = null;

    private void sendInvitationEmail(String email, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("You're invited to join!");
        message.setText("Click the link to complete your signup: " + link
                + "\n\nThis invitation expires in " + INVITATION_VALIDITY_DAYS + " days.");
        mailSender.send(message);
    }
}
