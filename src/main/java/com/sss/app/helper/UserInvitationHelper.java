package com.sss.app.helper;

import com.sss.app.entity.notification.NotificationType;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.roles.Role;
import com.sss.app.entity.users.User;
import com.sss.app.entity.users.invitations.UserInvitation;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.InvitationTokenRepository;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.RoleRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.email.EmailService;
import com.sss.app.service.notification.NotificationService;
import com.sss.app.service.quotationtemplate.QuotationRenderingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserInvitationHelper {

    private static final long INVITATION_VALIDITY_DAYS = 15;
    private static final String EMAIL_BODY_TEMPLATE = "email-templates/user-invitation-email.mustache";

    private final InvitationTokenRepository invitationRepository;
    private final RoleRepository roleRepository;
    private final OrgAccessGuard orgAccessGuard;
    private final OrganizationRepository organizationRepository;
    private final QuotationRenderingService quotationRenderingService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public UserInvitationHelper(InvitationTokenRepository invitationRepository, RoleRepository roleRepository,
                                 OrgAccessGuard orgAccessGuard, OrganizationRepository organizationRepository,
                                 QuotationRenderingService quotationRenderingService, EmailService emailService,
                                 NotificationService notificationService) {
        this.invitationRepository = invitationRepository;
        this.roleRepository = roleRepository;
        this.orgAccessGuard = orgAccessGuard;
        this.organizationRepository = organizationRepository;
        this.quotationRenderingService = quotationRenderingService;
        this.emailService = emailService;
        this.notificationService = notificationService;
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
        sendInvitationEmail(email, link, inviter.getOrgId());

        notificationService.notifyUsers(notificationService.resolveOrgManagers(inviter.getOrgId()), inviter.getOrgId(),
                NotificationType.USER_INVITED, "User Invited",
                email + " has been invited to join your organization.",
                null, null);

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

    // Synchronous, inside inviteUser()'s own transaction — same as the
    // plain-text SimpleMailMessage this replaced. A send failure here still
    // propagates and rolls back the invitation row, unchanged from before;
    // only the email's own content/format changed.
    private void sendInvitationEmail(String email, String link, Long orgId) {
        Organizations org = organizationRepository.findById(orgId).orElse(null);
        String orgName = org != null
                ? (org.getDisplayName() != null && !org.getDisplayName().isBlank() ? org.getDisplayName() : org.getRegisteredName())
                : "";

        Map<String, Object> data = new HashMap<>();
        data.put("organizationName", orgName);
        data.put("invitationLink", link);
        data.put("expiryDays", INVITATION_VALIDITY_DAYS);

        String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
        emailService.sendHtmlEmail(List.of(email), "You're invited to join " + orgName, body);
    }
}
