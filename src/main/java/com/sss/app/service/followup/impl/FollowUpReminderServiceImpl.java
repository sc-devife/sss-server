package com.sss.app.service.followup.impl;

import com.sss.app.entity.followup.FollowUp;
import com.sss.app.entity.followup.FollowUpStatus;
import com.sss.app.entity.notification.NotificationType;
import com.sss.app.repository.followup.FollowUpRepository;
import com.sss.app.service.followup.FollowUpEmailService;
import com.sss.app.service.followup.FollowUpReminderService;
import com.sss.app.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Runs every minute (unlike the daily-cron AutoCancellationServiceImpl/
// PaymentReminderServiceImpl sweeps) — a 30-/15-minute-before reminder needs
// minute-level precision. Each window is +/-1 minute wide to absorb any
// drift between the scheduler tick and a follow-up's exact dueAt; the
// reminder30SentAt/reminder15SentAt columns (stamped immediately after a
// successful send) are what actually prevent a duplicate send if the same
// row is picked up by more than one run.
@Service
@RequiredArgsConstructor
@Slf4j
public class FollowUpReminderServiceImpl implements FollowUpReminderService {

    private final FollowUpRepository followUpRepository;
    private final FollowUpEmailService followUpEmailService;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void scheduledRun() {
        int thirty = sendThirtyMinuteReminders();
        int fifteen = sendFifteenMinuteReminders();
        int overdue = sendOverdueNotifications();
        if (thirty > 0 || fifteen > 0 || overdue > 0) {
            log.info("Follow-up reminder sweep sent {} 30-min, {} 15-min reminder(s), {} overdue notification(s)", thirty, fifteen, overdue);
        }
    }

    // FOLLOWUP_DUE_SOON covers both the 30- and 15-minute-before windows —
    // the emails stay separate (different lead time), but one notification
    // type is enough for the in-app bell.
    private void notifyDueSoon(FollowUp followUp) {
        notificationService.notify(followUp.getAssignedTo().getSeqp(), followUp.getOrgId(),
                NotificationType.FOLLOWUP_DUE_SOON, "Follow-up Due Soon",
                "\"" + followUp.getComment() + "\" is due soon.",
                NotificationType.RelatedEntityType.FOLLOWUP, followUp.getUid());
    }

    @Transactional
    public int sendOverdueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<FollowUp> overdue = followUpRepository.findAllByActionableTrueAndStatusNotAndDueAtBeforeAndOverdueNotifiedAtIsNull(
                FollowUpStatus.COMPLETED, now);
        int sent = 0;
        for (FollowUp followUp : overdue) {
            try {
                notificationService.notify(followUp.getAssignedTo().getSeqp(), followUp.getOrgId(),
                        NotificationType.FOLLOWUP_OVERDUE, "Follow-up Overdue",
                        "\"" + followUp.getComment() + "\" is now overdue.",
                        NotificationType.RelatedEntityType.FOLLOWUP, followUp.getUid());
                followUp.setOverdueNotifiedAt(now);
                followUpRepository.save(followUp);
                sent++;
            } catch (Exception e) {
                log.error("Follow-up reminder sweep: failed overdue notification for {}", followUp.getUid(), e);
            }
        }
        return sent;
    }

    @Override
    @Transactional
    public int sendThirtyMinuteReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<FollowUp> due = followUpRepository.findAllByActionableTrueAndStatusNotAndDueAtBetweenAndReminder30SentAtIsNull(
                FollowUpStatus.COMPLETED, now.plusMinutes(29), now.plusMinutes(31));
        int sent = 0;
        for (FollowUp followUp : due) {
            try {
                followUpEmailService.sendReminderEmail(followUp, 30);
                notifyDueSoon(followUp);
                followUp.setReminder30SentAt(now);
                followUpRepository.save(followUp);
                sent++;
            } catch (Exception e) {
                log.error("Follow-up reminder sweep: failed 30-min reminder for {}", followUp.getUid(), e);
            }
        }
        return sent;
    }

    @Override
    @Transactional
    public int sendFifteenMinuteReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<FollowUp> due = followUpRepository.findAllByActionableTrueAndStatusNotAndDueAtBetweenAndReminder15SentAtIsNull(
                FollowUpStatus.COMPLETED, now.plusMinutes(14), now.plusMinutes(16));
        int sent = 0;
        for (FollowUp followUp : due) {
            try {
                followUpEmailService.sendReminderEmail(followUp, 15);
                notifyDueSoon(followUp);
                followUp.setReminder15SentAt(now);
                followUpRepository.save(followUp);
                sent++;
            } catch (Exception e) {
                log.error("Follow-up reminder sweep: failed 15-min reminder for {}", followUp.getUid(), e);
            }
        }
        return sent;
    }
}
