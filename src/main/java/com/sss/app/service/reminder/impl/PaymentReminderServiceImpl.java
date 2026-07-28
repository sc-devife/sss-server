package com.sss.app.service.reminder.impl;

import com.sss.app.dto.reminder.ReminderRuleCreateRequestDTO;
import com.sss.app.dto.reminder.ReminderRuleResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.payment.PaymentMilestone;
import com.sss.app.entity.reminder.PaymentReminderLog;
import com.sss.app.entity.reminder.ReminderRule;
import com.sss.app.helper.reminder.ReminderRuleHelper;
import com.sss.app.mapper.reminder.ReminderRuleMapper;
import com.sss.app.repository.payment.PaymentMilestoneRepository;
import com.sss.app.repository.reminder.PaymentReminderLogRepository;
import com.sss.app.repository.reminder.ReminderRuleRepository;
import com.sss.app.service.reminder.DefaultReminderCadence;
import com.sss.app.service.reminder.PaymentReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReminderServiceImpl implements PaymentReminderService {

    private static final List<String> OPEN_STATUSES = List.of("pending", "partially_paid", "overdue");

    private final ReminderRuleHelper reminderRuleHelper;
    private final ReminderRuleMapper reminderRuleMapper;
    private final ReminderRuleRepository reminderRuleRepository;
    private final PaymentMilestoneRepository paymentMilestoneRepository;
    private final PaymentReminderLogRepository paymentReminderLogRepository;
    private final JavaMailSender mailSender;

    @Override
    public List<ReminderRuleResponseDTO> getRulesForOrg() {
        List<ReminderRuleResponseDTO> saved = reminderRuleHelper.getAllForOrg().stream()
                .map(reminderRuleMapper::toResponse)
                .toList();
        if (!saved.isEmpty()) {
            return saved;
        }
        // No custom rules configured — show the recommended defaults that
        // are actually in effect, so the screen never looks empty/broken.
        return DefaultReminderCadence.RULES.stream().map(d -> {
            ReminderRuleResponseDTO dto = new ReminderRuleResponseDTO();
            dto.setLabel(d.label());
            dto.setOffsetDays(d.offsetDays());
            dto.setRecurring(d.recurring());
            dto.setIsActive(true);
            dto.setFallback(true);
            return dto;
        }).toList();
    }

    @Override
    public ReminderRuleResponseDTO createRule(ReminderRuleCreateRequestDTO request) {
        return reminderRuleMapper.toResponse(reminderRuleHelper.create(request));
    }

    @Override
    public void deleteRule(UUID uid) {
        reminderRuleHelper.delete(uid);
    }

    // Once a day, at 8am server time.
    @Scheduled(cron = "0 0 8 * * *")
    public void scheduledRun() {
        int sent = runDailyReminders();
        log.info("Payment reminder sweep sent {} reminder(s)", sent);
    }

    @Override
    @Transactional
    public int runDailyReminders() {
        LocalDate today = LocalDate.now();
        List<PaymentMilestone> openMilestones = paymentMilestoneRepository.findAllByStatusIn(OPEN_STATUSES);

        Map<Long, List<ReminderRule>> rulesByOrg = openMilestones.stream()
                .map(PaymentMilestone::getOrgId)
                .distinct()
                .collect(Collectors.toMap(orgId -> orgId, reminderRuleRepository::findAllByOrgIdAndIsActiveTrue));

        int sentCount = 0;
        for (PaymentMilestone milestone : openMilestones) {
            long diff = ChronoUnit.DAYS.between(milestone.getDueDate(), today);

            if (diff > 0 && "pending".equals(milestone.getStatus())) {
                milestone.setStatus("overdue");
                paymentMilestoneRepository.save(milestone);
            }

            List<ReminderRule> orgRules = rulesByOrg.getOrDefault(milestone.getOrgId(), List.of());
            boolean matches = orgRules.isEmpty()
                    ? matchesDefaultCadence(diff)
                    : orgRules.stream().anyMatch(rule -> matchesRule(rule.getOffsetDays(), rule.getRecurring(), diff));

            if (!matches) continue;
            if (paymentReminderLogRepository.existsByMilestone_SeqpAndSentDate(milestone.getSeqp(), today)) continue;

            if (sendReminder(milestone, diff)) {
                paymentReminderLogRepository.save(PaymentReminderLog.builder().milestone(milestone).sentDate(today).build());
                sentCount++;
            }
        }
        return sentCount;
    }

    private boolean matchesDefaultCadence(long diff) {
        return DefaultReminderCadence.RULES.stream().anyMatch(r -> matchesRule(r.offsetDays(), r.recurring(), diff));
    }

    private boolean matchesRule(int offsetDays, boolean recurring, long diff) {
        if (recurring) {
            return offsetDays > 0 && diff > 0 && diff % offsetDays == 0;
        }
        return diff == offsetDays;
    }

    private boolean sendReminder(PaymentMilestone milestone, long diff) {
        Escape trip = milestone.getDeal().getEscape();
        String recipientEmail = trip.getLead() != null ? trip.getLead().getEmail() : null;
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("Skipping payment reminder for milestone {} — no contact email on the trip's lead", milestone.getUid());
            return false;
        }

        String subject = diff > 0
                ? "Payment overdue: " + milestone.getLabel()
                : diff == 0
                ? "Payment due today: " + milestone.getLabel()
                : "Upcoming payment due: " + milestone.getLabel();

        String body = "Hi,\n\nThis is a reminder for your payment \"" + milestone.getLabel() + "\" of $"
                + milestone.getAmountUsd().subtract(milestone.getAmountPaidUsd()) + " USD remaining, due " + milestone.getDueDate() + ".\n\n"
                + "Please get in touch if you have any questions.";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send payment reminder for milestone {}", milestone.getUid(), e);
            return false;
        }
    }
}
