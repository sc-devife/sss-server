package com.sss.app.service.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.service.integration.ChannelLeadResult;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.integration.ProviderLeadMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LeadService {
    LeadResponseDTO createLead(LeadCreateRequestDTO request);
    LeadResponseDTO createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload);

    /** Lead Source Integration (Meta) path — adds dedup + provider metadata persistence. */
    ChannelLeadResult createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload, ProviderLeadMetadata sourceMetadata);

    LeadResponseDTO getLeadById(UUID id);

    /**
     * Leads page's Search + Status + Priority + Month/Week/Day/All +
     * pagination, applied as one DB-level query. `search`/`status` are
     * optional (null/blank = not applied); `priority` true adds the
     * Priority-pseudo-status filter; `start`/`end` back the date-period
     * filter (start inclusive, end exclusive) — both null means "All" (no
     * date restriction).
     */
    Page<LeadResponseDTO> getAllLeads(String search, String status, Boolean priority, LocalDateTime start, LocalDateTime end,
                                       String escapePointId, List<String> sources, Boolean archived, Pageable pageable);
    LeadResponseDTO updateLead(UUID id, LeadCreateRequestDTO request);
    LeadResponseDTO setFollowUpDueDate(UUID id, LocalDate followUpDueDate);

    /** Manual (re)assignment — see LeadAssignmentService.manuallyAssignLead. */
    LeadResponseDTO assignLead(UUID id, Long userId, String reason);

    // Internal-only: resolves the external uid to the entity's internal
    // seqp for callers (e.g. audit log lookups) that must keep using the
    // Long-keyed AuditLog storage without leaking seqp through the response DTO.
    Long resolveSeqp(UUID id);
}
