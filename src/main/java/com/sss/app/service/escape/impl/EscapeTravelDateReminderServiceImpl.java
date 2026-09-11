package com.sss.app.service.escape.impl;

import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.notification.NotificationType;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// "Escape.travel date approaching" notification — mirrors AutoCancellationServiceImpl's
// daily-cron/try-catch-per-item sweep shape exactly. Fires once, 3 days
// before an escape's startDate, for any escape that hasn't started yet
// (before Ongoing); the travelDateReminderSentAt stamp is the dedup guard,
// same role FollowUp's own reminder-sent columns play.
@Service
@RequiredArgsConstructor
@Slf4j
public class EscapeTravelDateReminderServiceImpl {

    private static final int DAYS_BEFORE = 3;

    // Every status strictly before Ongoing — an escape already underway or
    // finished has nothing "approaching" left to say.
    private static final List<String> IN_PROGRESS_STATUSES = new ArrayList<>(
            EscapeStatus.ORDER.subList(0, EscapeStatus.indexOf(EscapeStatus.ONGOING)));

    private final EscapeRepository escapeRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void scheduledRun() {
        int sent = sendTravelDateApproachingNotifications();
        if (sent > 0) {
            log.info("Travel-date-approaching sweep sent {} notification(s)", sent);
        }
    }

    @Transactional
    public int sendTravelDateApproachingNotifications() {
        LocalDate targetDate = LocalDate.now().plusDays(DAYS_BEFORE);
        List<Escape> approaching = escapeRepository.findAllByStatusInAndStartDateAndTravelDateReminderSentAtIsNull(
                IN_PROGRESS_STATUSES, targetDate);

        int sent = 0;
        for (Escape escape : approaching) {
            try {
                Long recipient = notificationService.resolveEscapeRecipient(escape);
                String tripCode = escape.getTripCode() != null ? escape.getTripCode() : escape.getUid().toString();
                String message = "Travel date for " + tripCode + " is approaching (" + escape.getStartDate() + ").";
                if (recipient != null) {
                    notificationService.notify(recipient, escape.getOrgId(),
                            NotificationType.ESCAPE_TRAVEL_DATE_APPROACHING, "Travel Date Approaching",
                            message, NotificationType.RelatedEntityType.ESCAPE, escape.getUid());
                } else {
                    notificationService.notifyUsers(notificationService.resolveOrgManagers(escape.getOrgId()), escape.getOrgId(),
                            NotificationType.ESCAPE_TRAVEL_DATE_APPROACHING, "Travel Date Approaching",
                            message, NotificationType.RelatedEntityType.ESCAPE, escape.getUid());
                }
                escape.setTravelDateReminderSentAt(LocalDateTime.now());
                escapeRepository.save(escape);
                sent++;
            } catch (Exception e) {
                log.error("Travel-date-approaching sweep: failed for escape {}", escape.getUid(), e);
            }
        }
        return sent;
    }
}
