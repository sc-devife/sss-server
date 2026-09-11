package com.sss.app.service.lead.impl;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.notification.NotificationType;
import com.sss.app.exception.ConflictException;
import com.sss.app.helper.lead.LeadsHelper;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.escape.EscapeService;
import com.sss.app.service.lead.LeadLifecycleService;
import com.sss.app.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private final NotificationService notificationService;

    @Override
    public LeadResponseDTO contact(UUID leadId) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireStatus(lead, "New");
        return transition(lead, "Contacted", "CONTACTED", null);
    }

    @Override
    public LeadResponseDTO qualify(UUID leadId) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireStatus(lead, "New", "Contacted");
        return transition(lead, "Qualified", "QUALIFIED", null);
    }

    @Override
    public LeadResponseDTO disqualify(UUID leadId, String reason) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireNotTerminal(lead);
        return transition(lead, "Unqualified", "DISQUALIFIED", reason);
    }

    @Override
    public LeadResponseDTO markLost(UUID leadId, String reason) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireNotTerminal(lead);
        return transition(lead, "Lost", "MARKED_LOST", reason);
    }

    @Override
    public LeadResponseDTO markDuplicate(UUID leadId, String reason) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireNotTerminal(lead);
        return transition(lead, "Duplicate", "MARKED_DUPLICATE", reason);
    }

    @Override
    public EscapeResponseDTO convertToEscape(UUID leadId, EscapeCreateRequestDTO request) {
        Lead lead = leadsHelper.getLeadById(leadId);
        requireStatus(lead, "Qualified");

        request.setLeadUid(leadId);
        EscapeResponseDTO escape = escapeService.createEscape(request);

        String previousStatus = lead.getStatus();
        lead.setStatus("Converted");
        leadRepository.save(lead);
        auditLogService.record(ENTITY_TYPE, lead.getSeqp(), "CONVERTED_TO_ESCAPE",
                previousStatus, "Converted -> Escape #" + escape.getUid());

        notificationService.notifyUsers(
                notificationService.resolveOrgManagers(lead.getOrgId()), lead.getOrgId(),
                NotificationType.LEAD_CONVERTED, "Lead Converted",
                lead.getName() + " has been converted to an Escape.",
                NotificationType.RelatedEntityType.ESCAPE, escape.getUid());

        return escape;
    }

    @Override
    public LeadResponseDTO togglePriority(UUID leadId) {
        Lead lead = leadsHelper.getLeadById(leadId);
        boolean newValue = !Boolean.TRUE.equals(lead.getIsPriority());
        lead.setIsPriority(newValue);
        Lead saved = leadRepository.save(lead);
        auditLogService.record(ENTITY_TYPE, lead.getSeqp(), "PRIORITY_TOGGLED", String.valueOf(!newValue), String.valueOf(newValue));
        return leadMapper.toResponse(saved);
    }

    @Override
    public void archive(UUID leadId) {
        Lead lead = leadsHelper.getLeadById(leadId);
        lead.setDeletedAt(java.time.LocalDateTime.now());
        leadRepository.save(lead);
        auditLogService.record(ENTITY_TYPE, lead.getSeqp(), "ARCHIVED", lead.getStatus(), "Archived");
    }

    private LeadResponseDTO transition(Lead lead, String newStatus, String action, String reason) {
        String previousStatus = lead.getStatus();
        lead.setStatus(newStatus);
        Lead saved = leadRepository.save(lead);
        auditLogService.record(ENTITY_TYPE, lead.getSeqp(), action, previousStatus, reason != null ? reason : newStatus);

        notificationService.notifyUsers(
                notificationService.resolveOrgManagers(saved.getOrgId()), saved.getOrgId(),
                NotificationType.LEAD_STATUS_CHANGED, "Lead Status Changed",
                saved.getName() + "'s status changed from " + previousStatus + " to " + newStatus + ".",
                NotificationType.RelatedEntityType.LEAD, saved.getUid());

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
