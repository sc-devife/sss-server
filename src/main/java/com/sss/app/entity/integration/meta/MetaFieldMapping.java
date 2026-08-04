package com.sss.app.entity.integration.meta;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Admin-editable mapping from a Meta field_data question key to a CRM Lead
 * field. formId null = org-wide default. See MetaFieldMappingResolver for
 * priority order (form-specific > org-default > hardcoded fallback).
 */
@Entity
@Table(name = "meta_field_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaFieldMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "form_id")
    private String formId;

    @Column(name = "meta_field_key", nullable = false)
    private String metaFieldKey;

    @Column(name = "crm_field", nullable = false)
    private String crmField;

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
