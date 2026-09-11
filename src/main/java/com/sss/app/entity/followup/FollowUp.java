package com.sss.app.entity.followup;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.users.User;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// A follow-up/task attached to exactly one of Lead or Escape (enforced by
// the follow_up_one_parent CHECK constraint, V94 migration), assigned to a
// User in the same org. actionable=false means it's a plain comment/note —
// it never appears in Pending/Overdue/Upcoming views, the header count, or
// reminder emails (see FollowUpSpecifications.isActionableAndOpen()).
// createdBy/createdAt/updatedBy/updatedAt come from Auditable — no separate
// "created by" column, same as every other Auditable entity in this codebase.
@Entity
@Table(name = "follow_ups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowUp extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escape_id")
    private Escape escape;

    @Column(length = 2000, nullable = false)
    private String comment;

    @Builder.Default
    @Column(nullable = false)
    private Boolean actionable = true;

    // Combined due date+time — nullable only when actionable=false (a pure
    // comment has nothing to be "due").
    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = FollowUpStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private User assignedTo;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Idempotency flags for the 30-/15-minute reminder sweep
    // (FollowUpReminderServiceImpl) — stamped once sent, never cleared, same
    // "stamp so a re-run is a no-op" role PaymentReminderLog plays for
    // payment reminders, just inlined as columns since each follow-up only
    // ever needs these two sends (no recurring cadence to track).
    @Column(name = "reminder_30_sent_at")
    private LocalDateTime reminder30SentAt;

    @Column(name = "reminder_15_sent_at")
    private LocalDateTime reminder15SentAt;

    // Dedup stamp for the "now overdue" notification (FOLLOWUP_OVERDUE) —
    // same role as the two reminder-sent columns above.
    @Column(name = "overdue_notified_at")
    private LocalDateTime overdueNotifiedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
