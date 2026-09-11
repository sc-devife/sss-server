package com.sss.app.service.notification.impl;

import com.sss.app.entity.notification.Notification;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.notification.NotificationRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.notification.sse.SseEmitterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// The repository's own derived-query filtering is Spring Data's
// responsibility (see AutoCancellationServiceImplTest's own note on this) —
// what's worth pinning down here is the Notification.RETENTION_DAYS cutoff
// this service computes and hands to the repository/entity boundary check,
// since that's hand-written logic that a future edit could silently drift.
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrgAccessGuard orgAccessGuard;
    @Mock private SseEmitterRegistry sseEmitterRegistry;

    private NotificationServiceImpl notificationService;

    @Captor private ArgumentCaptor<LocalDateTime> cutoffCaptor;

    private static final Long USER_SEQP = 1L;
    private static final Long ORG_ID = 100L;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository, userRepository, orgAccessGuard, sseEmitterRegistry);
        User currentUser = User.builder().seqp(USER_SEQP).orgId(ORG_ID).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllForCurrentUser_passesACutoffOfExactlyRetentionDaysAgo_totheRepository() {
        when(notificationRepository.findAllByRecipientUser_SeqpAndOrgIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                eq(USER_SEQP), eq(ORG_ID), cutoffCaptor.capture(), any(Pageable.class)))
                .thenReturn(Page.empty());

        notificationService.getAllForCurrentUser(Pageable.unpaged());

        LocalDateTime expected = LocalDateTime.now().minusDays(Notification.RETENTION_DAYS);
        assertThat(cutoffCaptor.getValue()).isCloseTo(expected, within(2, ChronoUnit.SECONDS));
    }

    @Test
    void countUnreadForCurrentUser_passesACutoffOfExactlyRetentionDaysAgo_totheRepository() {
        when(notificationRepository.countByRecipientUser_SeqpAndOrgIdAndIsReadFalseAndCreatedAtGreaterThanEqual(
                eq(USER_SEQP), eq(ORG_ID), cutoffCaptor.capture()))
                .thenReturn(0L);

        notificationService.countUnreadForCurrentUser();

        LocalDateTime expected = LocalDateTime.now().minusDays(Notification.RETENTION_DAYS);
        assertThat(cutoffCaptor.getValue()).isCloseTo(expected, within(2, ChronoUnit.SECONDS));
    }

    @Test
    void markAsRead_aNotificationOneDayInsideTheRetentionWindow_isStillMarkable() {
        Notification notification = notificationCreatedAgo(java.time.Duration.ofDays(Notification.RETENTION_DAYS - 1));
        when(notificationRepository.findByUid(notification.getUid())).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notification.getUid());

        assertThat(notification.getIsRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    // The cutoff is `now - RETENTION_DAYS`, re-evaluated at call time, so a
    // notification created a few minutes short of the full window is still
    // on the visible side of the line — proves the check isn't rounding to
    // whole days.
    @Test
    void markAsRead_justUnderTheRetentionWindow_isStillWithinRetention() {
        Notification notification = notificationCreatedAgo(java.time.Duration.ofDays(Notification.RETENTION_DAYS).minusMinutes(5));
        when(notificationRepository.findByUid(notification.getUid())).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notification.getUid());

        assertThat(notification.getIsRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    // Matches the cleanup sweep's own rule (createdAt < now - RETENTION_DAYS)
    // — once a notification reaches the retention age it's on the excluded
    // side for both reads and the delete sweep, never one without the other.
    @Test
    void markAsRead_atOrPastTheRetentionWindow_isTreatedAsNotFound_andNeverSaved() {
        Notification notification = notificationCreatedAgo(java.time.Duration.ofDays(Notification.RETENTION_DAYS).plusMinutes(5));
        when(notificationRepository.findByUid(notification.getUid())).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(notification.getUid()))
                .isInstanceOf(NotFoundException.class);
        verify(notificationRepository, never()).save(any());
    }

    private Notification notificationCreatedAgo(java.time.Duration age) {
        User recipient = User.builder().seqp(USER_SEQP).orgId(ORG_ID).build();
        Notification notification = Notification.builder()
                .uid(UUID.randomUUID())
                .orgId(ORG_ID)
                .recipientUser(recipient)
                .type("FOLLOWUP_OVERDUE")
                .title("Follow-up Overdue")
                .message("test")
                .isRead(false)
                .build();
        notification.setCreatedAt(LocalDateTime.now().minus(age));
        return notification;
    }
}
