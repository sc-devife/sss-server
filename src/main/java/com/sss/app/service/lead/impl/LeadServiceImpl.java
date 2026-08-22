package com.sss.app.service.lead.impl;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.helper.lead.LeadsHelper;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.service.assignment.LeadAssignmentService;
import com.sss.app.service.integration.ChannelLeadResult;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.integration.ProviderLeadMetadata;
import com.sss.app.service.lead.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadMapper leadMapper;
    private final LeadsHelper leadHelper;
    private final LeadAssignmentService leadAssignmentService;
    private final OrganizationRepository organizationRepository;

    @Override
    public LeadResponseDTO createLead(LeadCreateRequestDTO payload) {
        Lead lead = leadHelper.createLead(payload);
        autoAssignIfEnabled(lead);
        return leadMapper.toResponse(lead);
    }

    @Override
    public LeadResponseDTO createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload) {
        Lead lead = leadHelper.createLeadFromChannel(orgId, channelCode, payload.getSourceRefId(),
                payload.getName(), payload.getEmail(), payload.getPhone(), payload.getDestinationHint(),
                payload.getTravelDate(), payload.getNumberOfPeople(), payload.getDurationDays());
        autoAssignIfEnabled(lead);
        return leadMapper.toResponse(lead);
    }

    @Override
    public ChannelLeadResult createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload, ProviderLeadMetadata sourceMetadata) {
        LeadsHelper.ChannelLeadResult result = leadHelper.createLeadFromChannel(orgId, channelCode, payload, sourceMetadata);
        if (!result.wasDuplicate()) {
            autoAssignIfEnabled(result.lead());
        }
        return new ChannelLeadResult(result.lead().getSeqp(), result.wasDuplicate());
    }

    /**
     * Section 5: "auto-assignment runs when a lead is created if the org has
     * it enabled; otherwise leads land in an unassigned queue." Leaves the
     * lead unassigned (not an error) if disabled, or if enabled but no
     * eligible agent is found.
     */
    private void autoAssignIfEnabled(Lead lead) {
        Organizations org = organizationRepository.findById(lead.getOrgId()).orElse(null);
        if (org != null && Boolean.FALSE.equals(org.getAutoAssignEnabled())) {
            return;
        }
        leadAssignmentService.autoAssign(lead);
    }

    @Override
    public LeadResponseDTO getLeadById(UUID id) {
        return leadMapper.toResponse(leadHelper.getLeadById(id));
    }

    @Override
    public List<LeadResponseDTO> getAllLeads() {
        return leadHelper.getAllLeads()
                .stream()
                .map(leadMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Long resolveSeqp(UUID id) {
        return leadHelper.getLeadById(id).getSeqp();
    }

    @Override
    public LeadResponseDTO setFollowUpDueDate(UUID id, LocalDate followUpDueDate) {
        return leadMapper.toResponse(leadHelper.setFollowUpDueDate(id, followUpDueDate));
    }
}
