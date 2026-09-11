package com.sss.app.service.lead.impl;

import com.sss.app.dto.lead.LeadAgencyDetailsDTO;
import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.lead.LeadAgencyDetails;
import com.sss.app.entity.lead.LeadSourceType;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.helper.lead.LeadsHelper;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.repository.lead.LeadAgencyDetailsRepository;
import com.sss.app.service.integration.ChannelLeadResult;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.integration.ProviderLeadMetadata;
import com.sss.app.service.lead.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadMapper leadMapper;
    private final LeadsHelper leadHelper;
    private final LeadAgencyDetailsRepository leadAgencyDetailsRepository;

    @Override
    public LeadResponseDTO createLead(LeadCreateRequestDTO payload) {
        Lead lead = leadHelper.createLead(payload);
        return enrichAgencyDetails(lead, toResponseWithEscapePoints(lead));
    }

    @Override
    public LeadResponseDTO createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload) {
        Lead lead = leadHelper.createLeadFromChannel(orgId, channelCode, payload.getSourceRefId(),
                payload.getName(), payload.getEmail(), payload.getPhone(), payload.getDestinationHint(),
                payload.getTravelDate(), payload.getNumberOfPeople(), payload.getDurationNights());
        return toResponseWithEscapePoints(lead);
    }

    @Override
    public ChannelLeadResult createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload, ProviderLeadMetadata sourceMetadata) {
        LeadsHelper.ChannelLeadResult result = leadHelper.createLeadFromChannel(orgId, channelCode, payload, sourceMetadata);
        return new ChannelLeadResult(result.lead().getSeqp(), result.wasDuplicate());
    }

    @Override
    public LeadResponseDTO getLeadById(UUID id) {
        Lead lead = leadHelper.getLeadById(id);
        return enrichAgencyDetails(lead, toResponseWithEscapePoints(lead));
    }

    @Override
    public LeadResponseDTO updateLead(UUID id, LeadCreateRequestDTO request) {
        Lead lead = leadHelper.updateLead(id, request);
        return enrichAgencyDetails(lead, toResponseWithEscapePoints(lead));
    }

    @Override
    public Page<LeadResponseDTO> getAllLeads(String search, String status, Boolean priority, LocalDateTime start, LocalDateTime end,
                                              String escapePointId, List<String> sources, Boolean archived, Pageable pageable) {
        // Page<T>.map preserves the pagination metadata (totalElements,
        // totalPages, ...) while transforming just the page's own content —
        // the enrichment below never runs against more than one page's worth
        // of leads.
        return leadHelper.getAllLeads(search, status, priority, start, end, escapePointId, sources, archived, pageable)
                .map(lead -> enrichAgencyDetails(lead, toResponseWithEscapePoints(lead)));
    }

    @Override
    public Long resolveSeqp(UUID id) {
        return leadHelper.getLeadById(id).getSeqp();
    }

    @Override
    public LeadResponseDTO setFollowUpDueDate(UUID id, LocalDate followUpDueDate) {
        return toResponseWithEscapePoints(leadHelper.setFollowUpDueDate(id, followUpDueDate));
    }

    // escapePoints is a Set<EscapePoint> on the entity but a List<String> of
    // uids on the DTO — MapStruct can't auto-map that (see LeadMapper's
    // ignore), so every response path funnels through here instead.
    private LeadResponseDTO toResponseWithEscapePoints(Lead lead) {
        LeadResponseDTO response = leadMapper.toResponse(lead);
        response.setEscapePointIds(lead.getEscapePoints().stream().map(EscapePoint::getUid).toList());
        return response;
    }

    // agencyDetails has no matching field on the Lead entity (it's a
    // separate 1:1 record) — see LeadMapper's ignore mappings.
    private LeadResponseDTO enrichAgencyDetails(Lead lead, LeadResponseDTO response) {
        if (!LeadSourceType.AGENCY.equals(lead.getSourceType())) {
            return response;
        }
        leadAgencyDetailsRepository.findById(lead.getSeqp())
                .ifPresent(details -> {
                    LeadAgencyDetailsDTO dto = new LeadAgencyDetailsDTO();
                    BeanUtils.copyProperties(details, dto);
                    response.setAgencyDetails(dto);
                });
        return response;
    }
}
