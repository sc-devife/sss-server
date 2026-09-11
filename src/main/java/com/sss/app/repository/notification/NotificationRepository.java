package com.sss.app.repository.notification;

import com.sss.app.entity.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByUid(UUID uid);

    // The `retentionCutoff` param (now - Notification.RETENTION_DAYS) excludes expired
    // notifications from both the list and the unread count — the same
    // cutoff the cleanup sweep deletes by, so a row is either fully visible
    // or fully gone, never "deleted but still counted" or vice versa.
    Page<Notification> findAllByRecipientUser_SeqpAndOrgIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long recipientUserSeqp, Long orgId, LocalDateTime retentionCutoff, Pageable pageable);

    long countByRecipientUser_SeqpAndOrgIdAndIsReadFalseAndCreatedAtGreaterThanEqual(
            Long recipientUserSeqp, Long orgId, LocalDateTime retentionCutoff);

    // Bulk "mark all as read" in one statement rather than loading every
    // unread row into memory and saving each individually.
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now " +
            "WHERE n.recipientUser.seqp = :userSeqp AND n.orgId = :orgId AND n.isRead = false")
    int markAllAsRead(@Param("userSeqp") Long userSeqp, @Param("orgId") Long orgId, @Param("now") LocalDateTime now);

    // Retention cleanup: a plain bulk delete, org-unscoped on purpose — it
    // only ever touches Notification rows (no cascade to any other table),
    // so applying it across every org in one statement is safe and avoids
    // an org-by-org loop for what is otherwise a single global sweep.
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :retentionCutoff")
    int deleteAllByCreatedAtBefore(@Param("retentionCutoff") LocalDateTime retentionCutoff);
}
