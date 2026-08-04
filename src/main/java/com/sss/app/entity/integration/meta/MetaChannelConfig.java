package com.sss.app.entity.integration.meta;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Meta (Facebook Page / Instagram Business Account) connection details, kept
 * out of the generic integration_connections table on purpose — see
 * V39__create_meta_channel_configs.sql. One row per IntegrationConnection.
 */
@Entity
@Table(name = "meta_channel_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaChannelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(name = "connection_id", nullable = false, unique = true)
    private Long connectionId;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(nullable = false)
    private String platform; // "facebook" | "instagram"

    @Column(name = "page_id")
    private String pageId;

    @Column(name = "ig_account_id")
    private String igAccountId;

    @Column(name = "page_name")
    private String pageName;

    @Column(name = "token_type", nullable = false)
    private String tokenType; // "manual_long_lived" today; Stage 2 adds "oauth"

    @Column(name = "encrypted_access_token", nullable = false, columnDefinition = "TEXT")
    private String encryptedAccessToken;

    @Column(name = "token_iv", nullable = false)
    private String tokenIv;

    @Column(name = "token_last_verified_at")
    private LocalDateTime tokenLastVerifiedAt;

    @Column(name = "connected_by_user_id")
    private Long connectedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
