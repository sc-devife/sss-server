package com.sss.app.entity.notification;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.users.User;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// One row per (event, recipient) — a multi-recipient event (e.g. LEAD_CREATED
// notifying every org manager) creates one row per recipient, never a single
// shared row, so read state is always per-user. uid generated the same way
// every other entity this session does; createdBy/createdAt/updatedBy/
// updatedAt come from Auditable.
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends Auditable {

    // Shared by the read path (NotificationServiceImpl) and the cleanup
    // sweep (NotificationCleanupServiceImpl) so both enforce the exact same
    // cutoff — a notification is either within the window on both, or on
    // neither, never visible-but-undeleted or deleted-but-still-counted.
    public static final int RETENTION_DAYS = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "related_entity_type", length = 30)
    private String relatedEntityType;

    @Column(name = "related_entity_uid")
    private UUID relatedEntityUid;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
