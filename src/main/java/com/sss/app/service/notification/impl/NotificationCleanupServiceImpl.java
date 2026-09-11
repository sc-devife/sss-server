package com.sss.app.service.notification.impl;

import com.sss.app.entity.notification.Notification;
import com.sss.app.repository.notification.NotificationRepository;
import com.sss.app.service.notification.NotificationCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Runs once a day, same cadence as AutoCancellationServiceImpl — retention
// cleanup isn't time-critical, so a daily sweep is enough to keep the table
// from growing unbounded. A plain bulk DELETE, org-unscoped (see the
// repository query's own comment) since this only ever touches Notification
// rows — no other CRM entity is read or written here.
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupServiceImpl implements NotificationCleanupService {

    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledRun() {
        int deleted = deleteExpiredNotifications();
        if (deleted > 0) {
            log.info("Notification retention sweep: deleted {} notification(s) older than {} days",
                    deleted, Notification.RETENTION_DAYS);
        }
    }

    @Override
    @Transactional
    public int deleteExpiredNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(Notification.RETENTION_DAYS);
        return notificationRepository.deleteAllByCreatedAtBefore(cutoff);
    }
}
