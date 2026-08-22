package com.sss.app.service.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.service.integration.ChannelLeadResult;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.integration.ProviderLeadMetadata;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeadService {
    LeadResponseDTO createLead(LeadCreateRequestDTO request);
    LeadResponseDTO createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload);

    /** Lead Source Integration (Meta) path — adds dedup + provider metadata persistence. */
    ChannelLeadResult createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload, ProviderLeadMetadata sourceMetadata);

    LeadResponseDTO getLeadById(UUID id);
    List<LeadResponseDTO> getAllLeads();
    LeadResponseDTO setFollowUpDueDate(UUID id, LocalDate followUpDueDate);

    // Internal-only: resolves the external uid to the entity's internal
    // seqp for callers (e.g. audit log lookups) that must keep using the
    // Long-keyed AuditLog storage without leaking seqp through the response DTO.
    Long resolveSeqp(UUID id);
}
