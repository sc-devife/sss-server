package com.sss.app.service.notification.impl;

import com.sss.app.entity.notification.Notification;
import com.sss.app.repository.notification.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCleanupServiceImplTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationCleanupServiceImpl cleanupService;

    @Captor private ArgumentCaptor<LocalDateTime> cutoffCaptor;

    @Test
    void deleteExpiredNotifications_deletesByACutoffOfExactlyRetentionDaysAgo_andReturnsTheDeletedCount() {
        when(notificationRepository.deleteAllByCreatedAtBefore(cutoffCaptor.capture())).thenReturn(3);

        int deleted = cleanupService.deleteExpiredNotifications();

        assertThat(deleted).isEqualTo(3);
        LocalDateTime expected = LocalDateTime.now().minusDays(Notification.RETENTION_DAYS);
        assertThat(cutoffCaptor.getValue()).isCloseTo(expected, within(2, ChronoUnit.SECONDS));
    }
}
