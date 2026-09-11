package com.sss.app.repository.followup;

import com.sss.app.entity.followup.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long>, JpaSpecificationExecutor<FollowUp> {

    Optional<FollowUp> findByUid(UUID uid);

    List<FollowUp> findAllByLead_SeqpOrderByCreatedAtDesc(Long leadSeqp);

    List<FollowUp> findAllByEscape_SeqpOrderByCreatedAtDesc(Long escapeSeqp);

    // Header badge — all my open (not Completed) actionable follow-ups,
    // any due date.
    long countByAssignedTo_SeqpAndOrgIdAndActionableTrueAndStatusNot(Long assignedToSeqp, Long orgId, String status);

    // Reminder sweep (FollowUpReminderServiceImpl) — open actionable
    // follow-ups whose dueAt falls inside the given window and haven't had
    // this particular reminder sent yet.
    List<FollowUp> findAllByActionableTrueAndStatusNotAndDueAtBetweenAndReminder30SentAtIsNull(
            String excludedStatus, LocalDateTime from, LocalDateTime to);

    List<FollowUp> findAllByActionableTrueAndStatusNotAndDueAtBetweenAndReminder15SentAtIsNull(
            String excludedStatus, LocalDateTime from, LocalDateTime to);

    // "Just became overdue" — dueAt already passed, not yet flagged.
    List<FollowUp> findAllByActionableTrueAndStatusNotAndDueAtBeforeAndOverdueNotifiedAtIsNull(
            String excludedStatus, LocalDateTime now);
}
