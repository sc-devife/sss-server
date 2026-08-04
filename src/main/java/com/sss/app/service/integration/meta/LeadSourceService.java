package com.sss.app.service.integration.meta;

import com.sss.app.dto.integration.IntegrationConnectionResponseDTO;
import com.sss.app.dto.integration.meta.LeadImportAttemptResponseDTO;
import com.sss.app.dto.integration.meta.MetaFieldMappingDTO;
import com.sss.app.dto.integration.meta.MetaWebhookEventResponseDTO;
import com.sss.app.entity.integration.meta.LeadImportAttempt;
import com.sss.app.entity.integration.meta.MetaFieldMapping;
import com.sss.app.entity.integration.meta.MetaWebhookEvent;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.integration.meta.LeadImportAttemptRepository;
import com.sss.app.repository.integration.meta.MetaFieldMappingRepository;
import com.sss.app.repository.integration.meta.MetaWebhookEventRepository;
import com.sss.app.service.integration.IntegrationConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Day-to-day operational surface for the Lead Sources module (sync history,
 * webhook/import logs, field-mapping edits) — connect/disconnect itself
 * stays on IntegrationConnectionService/Controller since that's a
 * security-sensitive admin action gated by organizations.write, not
 * leads.write. This service is intentionally thin/read-mostly.
 */
@Service
@RequiredArgsConstructor
public class LeadSourceService {

    private static final Set<String> META_CHANNELS = Set.of("facebook", "instagram");

    private final IntegrationConnectionService integrationConnectionService;
    private final MetaWebhookEventRepository webhookEventRepository;
    private final LeadImportAttemptRepository importAttemptRepository;
    private final MetaFieldMappingRepository fieldMappingRepository;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void requireMetaChannel(String channelCode) {
        if (!META_CHANNELS.contains(channelCode)) {
            throw new BadRequestException("Unknown Meta lead source channel: " + channelCode);
        }
    }

    public List<IntegrationConnectionResponseDTO> listMetaSources() {
        return integrationConnectionService.listForOrg().stream()
                .filter(dto -> META_CHANNELS.contains(dto.getChannelCode()))
                .toList();
    }

    public Page<MetaWebhookEventResponseDTO> listWebhookEvents(String channelCode, Pageable pageable) {
        requireMetaChannel(channelCode);
        Long orgId = currentUser().getOrgId();
        return webhookEventRepository.findAllByOrgIdAndPlatformOrderByReceivedAtDesc(orgId, channelCode, pageable)
                .map(this::toDto);
    }

    public Page<LeadImportAttemptResponseDTO> listImportAttempts(String channelCode, Pageable pageable) {
        requireMetaChannel(channelCode);
        Long orgId = currentUser().getOrgId();
        return importAttemptRepository.findAllByOrgIdAndProviderOrderByLastAttemptedAtDesc(orgId, channelCode, pageable)
                .map(this::toDto);
    }

    public List<MetaFieldMappingDTO> listFieldMappings(String channelCode) {
        requireMetaChannel(channelCode);
        Long orgId = currentUser().getOrgId();
        return fieldMappingRepository.findAllByOrgId(orgId).stream().map(this::toDto).toList();
    }

    public MetaFieldMappingDTO saveFieldMapping(String channelCode, MetaFieldMappingDTO request) {
        requireMetaChannel(channelCode);
        if (request.getMetaFieldKey() == null || request.getMetaFieldKey().isBlank()) {
            throw new BadRequestException("metaFieldKey is required");
        }
        if (request.getCrmField() == null || request.getCrmField().isBlank()) {
            throw new BadRequestException("crmField is required");
        }
        Long orgId = currentUser().getOrgId();

        MetaFieldMapping mapping = (request.getSeqp() != null
                ? fieldMappingRepository.findById(request.getSeqp())
                        .filter(m -> m.getOrgId().equals(orgId))
                        .orElseThrow(() -> new BadRequestException("Field mapping not found"))
                : MetaFieldMapping.builder().orgId(orgId).build());

        mapping.setFormId(request.getFormId());
        mapping.setMetaFieldKey(request.getMetaFieldKey());
        mapping.setCrmField(request.getCrmField());
        return toDto(fieldMappingRepository.save(mapping));
    }

    private MetaWebhookEventResponseDTO toDto(MetaWebhookEvent event) {
        MetaWebhookEventResponseDTO dto = new MetaWebhookEventResponseDTO();
        dto.setSeqp(event.getSeqp());
        dto.setPlatform(event.getPlatform());
        dto.setPageId(event.getPageId());
        dto.setLeadgenId(event.getLeadgenId());
        dto.setFormId(event.getFormId());
        dto.setAdId(event.getAdId());
        dto.setSignatureValid(event.getSignatureValid());
        dto.setProcessingStatus(event.getProcessingStatus());
        dto.setErrorMessage(event.getErrorMessage());
        dto.setRawPayload(event.getRawPayload());
        dto.setReceivedAt(event.getReceivedAt());
        return dto;
    }

    public LeadImportAttemptResponseDTO toResponseDto(LeadImportAttempt attempt) {
        return toDto(attempt);
    }

    private LeadImportAttemptResponseDTO toDto(LeadImportAttempt attempt) {
        LeadImportAttemptResponseDTO dto = new LeadImportAttemptResponseDTO();
        dto.setSeqp(attempt.getSeqp());
        dto.setProvider(attempt.getProvider());
        dto.setLeadgenId(attempt.getLeadgenId());
        dto.setFormId(attempt.getFormId());
        dto.setPageId(attempt.getPageId());
        dto.setAdId(attempt.getAdId());
        dto.setCampaignId(attempt.getCampaignId());
        dto.setStatus(attempt.getStatus());
        dto.setLeadId(attempt.getLeadId());
        dto.setFailureReason(attempt.getFailureReason());
        dto.setRetryCount(attempt.getRetryCount());
        dto.setLastAttemptedAt(attempt.getLastAttemptedAt());
        dto.setCreatedAt(attempt.getCreatedAt());
        return dto;
    }

    private MetaFieldMappingDTO toDto(MetaFieldMapping mapping) {
        MetaFieldMappingDTO dto = new MetaFieldMappingDTO();
        dto.setSeqp(mapping.getSeqp());
        dto.setFormId(mapping.getFormId());
        dto.setMetaFieldKey(mapping.getMetaFieldKey());
        dto.setCrmField(mapping.getCrmField());
        return dto;
    }
}
