package com.sss.app.entity.reminder;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reminder_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(nullable = false)
    private String label;

    @Column(name = "offset_days", nullable = false)
    private Integer offsetDays;

    @Column(nullable = false)
    private Boolean recurring;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.recurring == null) {
            this.recurring = false;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
