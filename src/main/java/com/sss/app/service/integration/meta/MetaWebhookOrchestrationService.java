package com.sss.app.service.integration.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sss.app.entity.integration.IntegrationConnection;
import com.sss.app.entity.integration.meta.LeadImportAttempt;
import com.sss.app.entity.integration.meta.MetaChannelConfig;
import com.sss.app.entity.integration.meta.MetaWebhookEvent;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.integration.IntegrationConnectionRepository;
import com.sss.app.repository.integration.meta.LeadImportAttemptRepository;
import com.sss.app.repository.integration.meta.MetaChannelConfigRepository;
import com.sss.app.repository.integration.meta.MetaWebhookEventRepository;
import com.sss.app.security.crypto.TokenEncryptionService;
import com.sss.app.service.integration.ChannelLeadResult;
import com.sss.app.service.integration.LeadChannelAdapter;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.integration.ProviderLeadMetadata;
import com.sss.app.service.integration.RemoteLeadFetcher;
import com.sss.app.service.lead.LeadService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Orchestrates the full Meta Lead Ads pipeline: parse webhook -> resolve
 * which org/connection owns the page -> idempotency check -> fetch full
 * lead via Graph API -> normalize -> dedup + create -> log outcome. Kept
 * separate from MetaWebhookController (transport concerns only) and from
 * LeadsHelper/LeadService (which stay provider-agnostic).
 */
@Service
@RequiredArgsConstructor
public class MetaWebhookOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(MetaWebhookOrchestrationService.class);

    private final ObjectMapper objectMapper;
    private final MetaChannelConfigRepository channelConfigRepository;
    private final IntegrationConnectionRepository connectionRepository;
    private final MetaWebhookEventRepository webhookEventRepository;
    private final LeadImportAttemptRepository importAttemptRepository;
    private final TokenEncryptionService tokenEncryptionService;
    private final List<RemoteLeadFetcher> leadFetchers;
    private final List<LeadChannelAdapter> leadChannelAdapters;
    private final LeadService leadService;

    public void handleWebhookDelivery(String rawBody, boolean signatureValid) {
        if (!signatureValid) {
            persistWebhookEvent(null, null, null, null, null, false, "rejected", "Invalid or missing signature", rawBody);
            return;
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            persistWebhookEvent(null, null, null, null, null, true, "error", "Malformed JSON: " + e.getMessage(), rawBody);
            return;
        }

        String object = root.path("object").asText(null);
        String platform = "instagram".equals(object) ? "instagram" : "facebook";

        for (JsonNode entry : root.path("entry")) {
            String pageId = entry.path("id").asText(null);
            for (JsonNode change : entry.path("changes")) {
                if (!"leadgen".equals(change.path("field").asText(null))) {
                    continue;
                }
                processLeadgenChange(platform, pageId, change.path("value"), rawBody);
            }
        }
    }

    private void processLeadgenChange(String platform, String pageId, JsonNode value, String rawBody) {
        String leadgenId = value.path("leadgen_id").asText(null);
        String formIdFromWebhook = value.path("form_id").asText(null);

        if (leadgenId == null) {
            persistWebhookEvent(null, platform, pageId, null, formIdFromWebhook, true, "rejected", "Missing leadgen_id", rawBody);
            return;
        }

        Optional<MetaChannelConfig> configOpt = resolveConfig(platform, pageId);
        if (configOpt.isEmpty()) {
            persistWebhookEvent(null, platform, pageId, leadgenId, formIdFromWebhook, true, "rejected",
                    "No connected organization owns page/account id " + pageId, rawBody);
            return;
        }
        MetaChannelConfig config = configOpt.get();
        Long orgId = config.getOrgId();

        IntegrationConnection connection = connectionRepository.findById(config.getConnectionId()).orElse(null);
        if (connection == null || !"connected".equals(connection.getStatus())) {
            persistWebhookEvent(orgId, platform, pageId, leadgenId, formIdFromWebhook, true, "rejected",
                    "Connection is disconnected", rawBody);
            return;
        }

        Optional<LeadImportAttempt> existingAttempt = importAttemptRepository.findByOrgIdAndLeadgenId(orgId, leadgenId);
        if (existingAttempt.isPresent() && !LeadImportAttempt.STATUS_FAILED.equals(existingAttempt.get().getStatus())) {
            // Meta redelivers the same event on retry/ack timeouts — already handled, skip re-processing.
            persistWebhookEvent(orgId, platform, pageId, leadgenId, formIdFromWebhook, true, "duplicate_skipped", null, rawBody);
            return;
        }

        if (!Boolean.TRUE.equals(connection.getAutoCreateLeads())) {
            persistWebhookEvent(orgId, platform, pageId, leadgenId, formIdFromWebhook, true, "rejected",
                    "Auto-create is disabled for this connection", rawBody);
            return;
        }

        try {
            attemptImport(config, connection, platform, pageId, leadgenId, existingAttempt.orElse(null));
            persistWebhookEvent(orgId, platform, pageId, leadgenId, formIdFromWebhook, true, "processed", null, rawBody);
        } catch (Exception e) {
            log.error("Failed to import Meta lead (leadgenId={}, orgId={})", leadgenId, orgId, e);
            persistWebhookEvent(orgId, platform, pageId, leadgenId, formIdFromWebhook, true, "error", e.getMessage(), rawBody);
        }
    }

    /**
     * The full fetch -> normalize -> dedup -> create pipeline, reused by both
     * the live webhook path above and MetaResyncService for manual retries.
     * On any failure, persists a `failed` LeadImportAttempt row (with reason)
     * before rethrowing, so nothing fails silently.
     */
    public LeadImportAttempt attemptImport(MetaChannelConfig config, IntegrationConnection connection,
                                            String platform, String pageId, String leadgenId,
                                            LeadImportAttempt existingAttempt) {
        try {
            RemoteLeadFetcher fetcher = resolveFetcher(platform);
            LeadChannelAdapter adapter = resolveAdapter(platform);

            String accessToken = tokenEncryptionService.decrypt(config.getEncryptedAccessToken(), config.getTokenIv());
            Map<String, Object> fetched = fetcher.fetchLead(leadgenId, accessToken);

            Map<String, Object> merged = new HashMap<>(fetched);
            merged.put("_orgId", config.getOrgId());
            merged.put("_pageId", pageId);
            merged.put("leadgen_id", leadgenId);

            NormalizedLeadPayload normalized = adapter.normalize(merged);

            ProviderLeadMetadata sourceMetadata = new ProviderLeadMetadata(
                    platform,
                    leadgenId,
                    normalized.getCampaignId(),
                    normalized.getCampaignName(),
                    asString(fetched.get("adset_id")),
                    normalized.getAdId(),
                    asString(fetched.get("ad_name")),
                    normalized.getFormId(),
                    null, // Graph API's leadgen node doesn't return a form name field
                    normalized.getPageId(),
                    normalized.getReceivedAt(),
                    toJson(fetched.get("field_data")));

            ChannelLeadResult result = leadService.createLeadFromChannel(config.getOrgId(), platform, normalized, sourceMetadata);

            LeadImportAttempt attempt = existingAttempt != null ? existingAttempt : LeadImportAttempt.builder()
                    .orgId(config.getOrgId())
                    .connectionId(connection.getSeqp())
                    .provider(platform)
                    .leadgenId(leadgenId)
                    .build();

            attempt.setFormId(normalized.getFormId());
            attempt.setPageId(pageId);
            attempt.setAdId(normalized.getAdId());
            attempt.setCampaignId(normalized.getCampaignId());
            attempt.setStatus(result.wasDuplicate() ? LeadImportAttempt.STATUS_DUPLICATE_MATCHED : LeadImportAttempt.STATUS_SUCCESS);
            attempt.setLeadId(result.leadId());
            attempt.setFailureReason(null);
            attempt.setRawMergedPayload(toJson(merged));
            attempt.setRetryCount(existingAttempt != null ? existingAttempt.getRetryCount() + 1 : 0);
            attempt.setLastAttemptedAt(LocalDateTime.now());
            importAttemptRepository.save(attempt);

            connection.setLastSyncedAt(LocalDateTime.now());
            connectionRepository.save(connection);

            return attempt;
        } catch (Exception e) {
            LeadImportAttempt attempt = existingAttempt != null ? existingAttempt : LeadImportAttempt.builder()
                    .orgId(config.getOrgId())
                    .connectionId(connection.getSeqp())
                    .provider(platform)
                    .leadgenId(leadgenId)
                    .build();
            attempt.setPageId(pageId);
            attempt.setStatus(LeadImportAttempt.STATUS_FAILED);
            attempt.setFailureReason(e.getMessage());
            attempt.setRetryCount(existingAttempt != null ? existingAttempt.getRetryCount() + 1 : 0);
            attempt.setLastAttemptedAt(LocalDateTime.now());
            importAttemptRepository.save(attempt);
            throw e;
        }
    }

    /** Re-runs the pipeline for an existing failed attempt row (MetaResyncService). */
    public LeadImportAttempt retryImport(LeadImportAttempt existingAttempt) {
        MetaChannelConfig config = channelConfigRepository.findByConnectionId(existingAttempt.getConnectionId())
                .orElseThrow(() -> new NotFoundException("Meta connection config not found"));
        IntegrationConnection connection = connectionRepository.findById(existingAttempt.getConnectionId())
                .orElseThrow(() -> new NotFoundException("Integration connection not found"));
        return attemptImport(config, connection, existingAttempt.getProvider(), existingAttempt.getPageId(),
                existingAttempt.getLeadgenId(), existingAttempt);
    }

    private Optional<MetaChannelConfig> resolveConfig(String platform, String pageId) {
        if (pageId == null) {
            return Optional.empty();
        }
        return "instagram".equals(platform)
                ? channelConfigRepository.findByIgAccountId(pageId)
                : channelConfigRepository.findByPageId(pageId);
    }

    private RemoteLeadFetcher resolveFetcher(String platform) {
        return leadFetchers.stream()
                .filter(f -> f.providerCode().equals(platform))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No RemoteLeadFetcher registered for platform " + platform));
    }

    private LeadChannelAdapter resolveAdapter(String platform) {
        return leadChannelAdapters.stream()
                .filter(a -> a.channelCode().equals(platform))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No LeadChannelAdapter registered for platform " + platform));
    }

    private void persistWebhookEvent(Long orgId, String platform, String pageId, String leadgenId, String formId,
                                      boolean signatureValid, String status, String errorMessage, String rawBody) {
        MetaWebhookEvent event = MetaWebhookEvent.builder()
                .orgId(orgId)
                .platform(platform)
                .pageId(pageId)
                .leadgenId(leadgenId)
                .formId(formId)
                .signatureValid(signatureValid)
                .processingStatus(status)
                .errorMessage(errorMessage)
                .rawPayload(rawBody)
                .build();
        webhookEventRepository.save(event);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }
}
