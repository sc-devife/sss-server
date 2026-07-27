package com.sss.app.controller.integration;

import com.sss.app.entity.integration.IntegrationConnection;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.integration.IntegrationConnectionRepository;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.integration.impl.WebhookLeadAdapter;
import com.sss.app.service.lead.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The one real, end-to-end wired lead channel (Section 7). Deliberately
 * public/unauthenticated — the calling system is an external webhook
 * sender, not a logged-in user — secured instead by a per-org shared secret
 * set when the org connects this channel (see IntegrationConnectionController).
 */
@RestController
@RequestMapping("/api/integrations/webhook")
@RequiredArgsConstructor
public class WebhookLeadController {

    private static final String CHANNEL_CODE = "webhook";

    private final OrganizationRepository organizationRepository;
    private final IntegrationConnectionRepository integrationConnectionRepository;
    private final WebhookLeadAdapter webhookLeadAdapter;
    private final LeadService leadService;

    @PostMapping("/{orgUid}/leads")
    public ResponseEntity<Map<String, Object>> receiveLead(@PathVariable String orgUid,
                                                              @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
                                                              @RequestBody Map<String, Object> rawPayload) {
        Organizations org = organizationRepository.findByUid(orgUid)
                .orElseThrow(() -> new NotFoundException("Unknown organization"));

        IntegrationConnection connection = integrationConnectionRepository
                .findByOrgIdAndChannelCode(org.getSeqp(), CHANNEL_CODE)
                .orElseThrow(() -> new ConflictException("This organization has not connected the webhook channel"));

        if (!"connected".equals(connection.getStatus())) {
            throw new ConflictException("The webhook channel is disconnected for this organization");
        }
        if (connection.getConfig() == null || !connection.getConfig().equals(secret)) {
            throw new ConflictException("Invalid or missing webhook secret");
        }
        if (!Boolean.TRUE.equals(connection.getAutoCreateLeads())) {
            return ResponseEntity.ok(Map.of("accepted", false, "reason", "auto-create is disabled for this channel"));
        }

        NormalizedLeadPayload normalized = webhookLeadAdapter.normalize(rawPayload);
        var lead = leadService.createLeadFromChannel(org.getSeqp(), CHANNEL_CODE, normalized);

        connection.setLastSyncedAt(LocalDateTime.now());
        integrationConnectionRepository.save(connection);

        return ResponseEntity.ok(Map.of("accepted", true, "leadId", lead.getSeqp()));
    }
}
