package com.sss.app.service.integration;

import java.time.LocalDateTime;

/**
 * Everything about a channel-sourced lead that's worth persisting for
 * support/reporting but that core Lead-creation logic never needs to read —
 * the fields that end up in LeadSourceMetadata, not in NormalizedLeadPayload
 * (which only carries what actually populates the Lead itself). Generic
 * across providers, not Meta-specific, so LeadsHelper/LeadService stay
 * provider-agnostic per the "don't couple Facebook/Instagram to the Lead
 * module" architecture requirement.
 */
public record ProviderLeadMetadata(
        String provider,
        String platformLeadId,
        String campaignId,
        String campaignName,
        String adsetId,
        String adId,
        String adName,
        String formId,
        String formName,
        String pageId,
        LocalDateTime receivedAt,
        String rawFieldData
) {}
