package com.sss.app.service.followup;

import com.sss.app.entity.followup.FollowUp;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.service.email.EmailService;
import com.sss.app.service.quotationtemplate.QuotationRenderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Both the immediate "assigned to you" email and the 30-/15-minute reminder
// emails share one template — only the heading/intro line differs — same
// "renderClasspathTemplate + sendHtmlEmail" pattern UserInvitationHelper
// already uses for the invitation email.
@Service
@RequiredArgsConstructor
@Slf4j
public class FollowUpEmailService {

    private static final String EMAIL_BODY_TEMPLATE = "email-templates/followup-notification-email.mustache";
    private static final DateTimeFormatter DUE_AT_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a");

    private final QuotationRenderingService quotationRenderingService;
    private final EmailService emailService;
    private final OrganizationRepository organizationRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendAssignmentEmail(FollowUp followUp) {
        send(followUp, "New follow-up assigned to you", "You've been assigned a new follow-up.");
    }

    public void sendReminderEmail(FollowUp followUp, int minutesBefore) {
        send(followUp, "Follow-up due in " + minutesBefore + " minutes",
                "This is a reminder that your follow-up is due in " + minutesBefore + " minutes.");
    }

    // A failed send here must never break the caller's own flow (creating a
    // follow-up, or the reminder sweep processing the next row) — caught and
    // logged, same as PaymentReminderServiceImpl.sendReminder's own try/catch.
    private void send(FollowUp followUp, String subject, String introText) {
        String email = followUp.getAssignedTo() != null ? followUp.getAssignedTo().getEmail() : null;
        if (email == null || email.isBlank()) {
            log.warn("Skipping follow-up email for {} — assignee has no email", followUp.getUid());
            return;
        }
        try {
            Organizations org = organizationRepository.findById(followUp.getOrgId()).orElse(null);
            String orgName = org != null
                    ? (org.getDisplayName() != null && !org.getDisplayName().isBlank() ? org.getDisplayName() : org.getRegisteredName())
                    : "";

            boolean isLead = followUp.getLead() != null;
            String recordLabel = isLead ? "Lead: " + followUp.getLead().getName() : "Escape: " + followUp.getEscape().getTripCode();
            String recordLink = isLead
                    ? frontendUrl + "/leads/" + followUp.getLead().getUid()
                    : frontendUrl + "/escapes/" + followUp.getEscape().getUid();

            Map<String, Object> data = new HashMap<>();
            data.put("organizationName", orgName);
            data.put("heading", subject);
            data.put("introText", introText);
            data.put("comment", followUp.getComment());
            data.put("recordLabel", recordLabel);
            data.put("recordLink", recordLink);
            data.put("hasDueAt", followUp.getDueAt() != null);
            if (followUp.getDueAt() != null) {
                data.put("dueAtDisplay", followUp.getDueAt().format(DUE_AT_FORMAT));
            }

            String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
            emailService.sendHtmlEmail(List.of(email), subject, body);
        } catch (Exception e) {
            log.error("Failed to send follow-up email for {}", followUp.getUid(), e);
        }
    }
}
