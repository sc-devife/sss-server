package com.sss.app.helper;

import com.sss.app.entity.roles.Role;
import com.sss.app.entity.users.User;
import com.sss.app.entity.users.invitations.UserInvitation;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.InvitationTokenRepository;
import com.sss.app.repository.RoleRepository;
import com.sss.app.security.OrgAccessGuard;
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
import java.util.List;

@Component
public class UserInvitationHelper {

    private static final long INVITATION_VALIDITY_DAYS = 15;

    private final InvitationTokenRepository invitationRepository;
    private final RoleRepository roleRepository;
    private final OrgAccessGuard orgAccessGuard;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public UserInvitationHelper(InvitationTokenRepository invitationRepository, RoleRepository roleRepository, OrgAccessGuard orgAccessGuard) {
        this.invitationRepository = invitationRepository;
        this.roleRepository = roleRepository;
        this.orgAccessGuard = orgAccessGuard;
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional
    public UserInvitation inviteUser(String email, List<String> roleNames) {
        invitationRepository.findByEmail(email)
                .filter(existing -> !existing.is_used() && !existing.is_archived() && existing.getExpires_set().isAfter(LocalDateTime.now()))
                .ifPresent(existing -> {
                    throw new ConflictException("An active invitation already exists for " + email);
                });

        List<Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }

        User inviter = currentUser();

        UserInvitation userInvitation = new UserInvitation();
        userInvitation.setEmail(email);
        userInvitation.setOrgId(inviter.getOrgId());
        userInvitation.setInvitedBy(inviter.getSeqp());
        userInvitation.setExpires_set(LocalDateTime.now().plusDays(INVITATION_VALIDITY_DAYS));
        userInvitation.set_used(false);
        userInvitation.set_archived(false);
        userInvitation.setRoles(roleNames);

        userInvitation = invitationRepository.save(userInvitation);
        entityManager.flush();
        entityManager.refresh(userInvitation);

        String link = frontendUrl + "/signup?token=" + userInvitation.getUid() + "&email=" + email;
        sendInvitationEmail(email, link);
        return userInvitation;
    }

    public List<UserInvitation> listPendingInvitations() {
        return invitationRepository.findPendingByOrgId(currentUser().getOrgId());
    }

    @Transactional
    public void cancelInvitation(Long invitationId) {
        UserInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation not found"));
        orgAccessGuard.requireAccessToOrg(invitation.getOrgId());

        if (invitation.is_used()) {
            throw new ConflictException("This invitation has already been redeemed");
        }

        invitation.set_archived(true);
        invitationRepository.save(invitation);
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
