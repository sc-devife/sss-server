package com.sss.app.entity.integration.meta;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Provider-specific lead metadata (campaign/ad/form), deliberately split out
 * of the `leads` table — see V40__create_lead_source_metadata.sql. Lead/
 * LeadRepository/LeadsHelper never reference this table; it exists purely
 * for the Lead Sources sync-history/log UI.
 */
@Entity
@Table(name = "lead_source_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadSourceMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(name = "lead_id", nullable = false, unique = true)
    private Long leadId;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(nullable = false)
    private String provider; // "facebook" | "instagram"

    @Column(name = "platform_lead_id")
    private String platformLeadId;

    @Column(name = "campaign_id")
    private String campaignId;

    @Column(name = "campaign_name")
    private String campaignName;

    @Column(name = "adset_id")
    private String adsetId;

    @Column(name = "ad_id")
    private String adId;

    @Column(name = "ad_name")
    private String adName;

    @Column(name = "form_id")
    private String formId;

    @Column(name = "form_name")
    private String formName;

    @Column(name = "page_id")
    private String pageId;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "raw_field_data", columnDefinition = "TEXT")
    private String rawFieldData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
