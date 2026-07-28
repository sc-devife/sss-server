package com.sss.app.service.lead.impl;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.exception.ConflictException;
import com.sss.app.helper.lead.LeadsHelper;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.escape.EscapeService;
import com.sss.app.service.lead.LeadLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class LeadLifecycleServiceImpl implements LeadLifecycleService {

    private static final String ENTITY_TYPE = "Lead";

    private final LeadsHelper leadsHelper;
    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;
    private final AuditLogService auditLogService;
    private final EscapeService escapeService;

    @Override
    public LeadResponseDTO contact(Long leadId) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireStatus(lead, "New");
        return transition(lead, "Contacted", "CONTACTED", null);
    }

    @Override
    public LeadResponseDTO qualify(Long leadId) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireStatus(lead, "New", "Contacted");
        return transition(lead, "Qualified", "QUALIFIED", null);
    }

    @Override
    public LeadResponseDTO disqualify(Long leadId, String reason) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireNotTerminal(lead);
        return transition(lead, "Unqualified", "DISQUALIFIED", reason);
    }

    @Override
    public LeadResponseDTO markLost(Long leadId, String reason) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireNotTerminal(lead);
        return transition(lead, "Lost", "MARKED_LOST", reason);
    }

    @Override
    public LeadResponseDTO markDuplicate(Long leadId, String reason) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireNotTerminal(lead);
        return transition(lead, "Duplicate", "MARKED_DUPLICATE", reason);
    }

    @Override
    public EscapeResponseDTO convertToEscape(Long leadId, EscapeCreateRequestDTO request) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireStatus(lead, "Qualified");

        request.setLeadId(leadId);
        EscapeResponseDTO escape = escapeService.createEscape(request);

        String previousStatus = lead.getStatus();
        lead.setStatus("Converted");
        leadRepository.save(lead);
        auditLogService.record(ENTITY_TYPE, leadId, "CONVERTED_TO_ESCAPE",
                previousStatus, "Converted -> Escape #" + escape.getSeqp());

        return escape;
    }

    @Override
    public LeadResponseDTO togglePriority(Long leadId) {
        Lead lead = leadsHelper.getLeadById(leadId);
        boolean newValue = !Boolean.TRUE.equals(lead.getIsPriority());
        lead.setIsPriority(newValue);
        Lead saved = leadRepository.save(lead);
        auditLogService.record(ENTITY_TYPE, leadId, "PRIORITY_TOGGLED", String.valueOf(!newValue), String.valueOf(newValue));
        return leadMapper.toResponse(saved);
    }

    private LeadResponseDTO transition(Lead lead, String newStatus, String action, String reason) {
        String previousStatus = lead.getStatus();
        lead.setStatus(newStatus);
        Lead saved = leadRepository.save(lead);
        auditLogService.record(ENTITY_TYPE, lead.getSeqp(), action, previousStatus, reason != null ? reason : newStatus);
        return leadMapper.toResponse(saved);
    }

    private void requireStatus(Lead lead, String... allowed) {
        Set<String> allowedSet = Set.of(allowed);
        if (!allowedSet.contains(lead.getStatus())) {
            throw new ConflictException("Lead is in status \"" + lead.getStatus() + "\" — this action requires one of: " + String.join(", ", allowed));
        }
    }

    private void requireNotTerminal(Lead lead) {
        List<String> terminal = List.of("Unqualified", "Lost", "Duplicate", "Converted");
        if (terminal.contains(lead.getStatus())) {
            throw new ConflictException("Lead is already in a terminal status: " + lead.getStatus());
        }
    }
}
