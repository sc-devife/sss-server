package com.sss.app.entity.integration.meta;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Raw delivery-level audit of every Meta webhook POST, valid or not — see
 * V41__create_meta_webhook_and_import_logs.sql. This is NOT the idempotency
 * guard (that's LeadImportAttempt's unique org+leadgenId); Meta's retries of
 * an already-processed delivery still get logged here every time.
 */
@Entity
@Table(name = "meta_webhook_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(name = "org_id")
    private Long orgId;

    @Column
    private String platform;

    @Column(name = "page_id")
    private String pageId;

    @Column(name = "leadgen_id")
    private String leadgenId;

    @Column(name = "form_id")
    private String formId;

    @Column(name = "ad_id")
    private String adId;

    @Column(name = "signature_valid", nullable = false)
    private Boolean signatureValid;

    @Column(name = "processing_status", nullable = false)
    private String processingStatus; // received/processed/duplicate_skipped/rejected/error

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "raw_payload", nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
}
