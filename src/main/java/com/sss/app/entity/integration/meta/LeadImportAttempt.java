package com.sss.app.entity.integration.meta;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One logical row per (org, leadgenId) — both the idempotency guard for
 * webhook processing and the resync source of truth. Updated in place on
 * each retry rather than appended, so "read the failed row, retry it" is a
 * single lookup by id. See V41__create_meta_webhook_and_import_logs.sql.
 */
@Entity
@Table(name = "lead_import_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadImportAttempt {

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_DUPLICATE_MATCHED = "duplicate_matched";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "connection_id", nullable = false)
    private Long connectionId;

    @Column(nullable = false)
    private String provider;

    @Column(name = "leadgen_id", nullable = false)
    private String leadgenId;

    @Column(name = "form_id")
    private String formId;

    @Column(name = "page_id")
    private String pageId;

    @Column(name = "ad_id")
    private String adId;

    @Column(name = "campaign_id")
    private String campaignId;

    @Column(nullable = false)
    private String status;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "raw_merged_payload", columnDefinition = "TEXT")
    private String rawMergedPayload;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_attempted_at", nullable = false)
    private LocalDateTime lastAttemptedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        if (lastAttemptedAt == null) {
            lastAttemptedAt = now;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
