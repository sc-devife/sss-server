package com.sss.app.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Thin, reusable wrapper over the already-configured JavaMailSender bean
 * (spring.mail.* in application.properties — SMTP is already live, used
 * today by UserService/UserInvitationHelper/PaymentReminderServiceImpl).
 * Those three all send plain-text SimpleMailMessage with no attachment;
 * this is the first sender in the codebase that needs an HTML body plus a
 * PDF attachment, hence MimeMessageHelper instead.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /** One email, multiple "To" recipients, one PDF attachment — never split into per-recipient sends. */
    public void sendHtmlEmailWithAttachment(List<String> to, String subject, String htmlBody,
                                             byte[] attachmentBytes, String attachmentFilename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.addAttachment(attachmentFilename, new ByteArrayResource(attachmentBytes));
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
            throw new IllegalStateException("Failed to send email — please try again.", e);
        }
    }

    /** Same as above, without an attachment — e.g. the payment-confirmation notification. */
    public void sendHtmlEmail(List<String> to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
            throw new IllegalStateException("Failed to send email — please try again.", e);
        }
    }
}
