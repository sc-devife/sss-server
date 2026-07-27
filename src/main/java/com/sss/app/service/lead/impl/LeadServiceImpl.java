package com.sss.app.service.lead.impl;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.helper.lead.LeadsHelper;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.service.assignment.LeadAssignmentService;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.lead.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadMapper leadMapper;
    private final LeadsHelper leadHelper;
    private final LeadAssignmentService leadAssignmentService;

    @Override
    public LeadResponseDTO createLead(LeadCreateRequestDTO payload) {
        Lead lead = leadHelper.createLead(payload);
        // Section 5: auto-assignment runs at creation time; leaves the lead
        // unassigned (not an error) if no eligible agent is found.
        leadAssignmentService.autoAssign(lead);
        return leadMapper.toResponse(lead);
    }

    @Override
    public LeadResponseDTO createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload) {
        Lead lead = leadHelper.createLeadFromChannel(orgId, channelCode, payload.getSourceRefId(),
                payload.getName(), payload.getEmail(), payload.getPhone(), payload.getDestinationHint(),
                payload.getTravelDate(), payload.getNumberOfPeople(), payload.getDurationDays());
        leadAssignmentService.autoAssign(lead);
        return leadMapper.toResponse(lead);
    }

    @Override
    public LeadResponseDTO getLeadById(Long id) {
        return leadMapper.toResponse(leadHelper.getLeadById(id));
    }

    @Override
    public List<LeadResponseDTO> getAllLeads() {
        return leadHelper.getAllLeads()
                .stream()
                .map(leadMapper::toResponse)
                .collect(Collectors.toList());
    }
}
