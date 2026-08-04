package com.sss.app.service.integration;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * The common inbound lead contract every channel adapter maps its own
 * payload shape into (Section 7) — core lead-creation logic only ever sees
 * this, never a channel's raw format.
 *
 * The provider/campaign/ad/form/pageId/receivedAt fields below are optional
 * and only populated by channels that have that concept (currently Meta) —
 * WebhookLeadAdapter and any future self-contained-webhook channel leave
 * them null and are unaffected by their presence.
 */
@Data
public class NormalizedLeadPayload {
    private String name;
    private String email;
    private String phone;
    private String destinationHint; // free-text destination as the channel sent it
    private LocalDate travelDate;
    private Integer numberOfPeople;
    private Integer durationDays;
    private String sourceRefId; // the channel's own id for this lead, for dedup/ack
    private Map<String, Object> rawPayload;

    // Provider-scoped metadata (Meta Lead Ads) — see LeadSourceMetadata.
    private String provider;
    private String campaignId;
    private String campaignName;
    private String adId;
    private String formId;
    private String pageId;
    private LocalDateTime receivedAt;

    // Free-text overflow for custom-question answers with no field mapping
    // configured yet — never silently dropped, see MetaFieldMappingResolver.
    private String notes;
}
