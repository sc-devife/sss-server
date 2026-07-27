package com.sss.app.service.assignment.impl;

import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.lead.LeadStatus;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.lead.LeadsHelper;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.service.assignment.LeadAssignmentService;
import com.sss.app.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * v1 scope, flagged: priority auto-detection only checks trip duration
 * (Section 15's durationDays >= 4, per the user's "minimum 4 nights" rule).
 * The fuller metro-city-origin / honeymoon-season / vacation-calendar rules
 * from the requirements doc need an admin-configurable priority calendar UI
 * that isn't built yet — manual "mark as priority" override (already wired
 * via Lead.isPriority on create) covers that gap for now.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LeadAssignmentServiceImpl implements LeadAssignmentService {

    private static final int PRIORITY_MIN_DURATION_DAYS = 4;

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final LeadsHelper leadsHelper;
    private final LeadMapper leadMapper;
    private final AuditLogService auditLogService;

    @Override
    public void autoAssign(Lead lead) {
        boolean alreadyMarkedPriority = Boolean.TRUE.equals(lead.getIsPriority());
        if (!alreadyMarkedPriority && lead.getDurationDays() != null && lead.getDurationDays() >= PRIORITY_MIN_DURATION_DAYS) {
            lead.setIsPriority(true);
        }

        List<User> candidates = userRepository.findUsersWithRoles(lead.getOrgId());

        boolean isPriority = Boolean.TRUE.equals(lead.getIsPriority());
        List<User> eligible = candidates.stream()
                .filter(u -> !isPriority || Boolean.TRUE.equals(u.getEligibleForPriorityLeads()))
                .toList();

        if (lead.getDestinationRef() != null) {
            Long destinationSeqp = lead.getDestinationRef().getSeqp();
            List<User> specialists = eligible.stream()
                    .filter(u -> Boolean.TRUE.equals(u.getIsSpecialist())
                            && u.getSpecialistDestinations() != null
                            && u.getSpecialistDestinations().contains(destinationSeqp))
                    .toList();
            if (!specialists.isEmpty()) {
                eligible = specialists;
            }
        }

        Optional<User> chosen = eligible.stream()
                .filter(u -> withinCapacity(u))
                .min(Comparator.comparingLong(this::openLeadCount));

        if (chosen.isEmpty()) {
            leadRepository.save(lead);
            auditLogService.record("Lead", lead.getSeqp(), "AUTO_ASSIGN_SKIPPED", null,
                    "No eligible agent available — left in the unassigned queue");
            return;
        }

        User assignee = chosen.get();
        lead.setAssignedToUserId(assignee.getSeqp());
        lead.setAssignmentReason(isPriority
                ? "Auto-assigned: priority lead, load-balanced among priority-eligible agents"
                : "Auto-assigned: round-robin load balancing");
        leadRepository.save(lead);
        auditLogService.record("Lead", lead.getSeqp(), "AUTO_ASSIGNED", null, assignee.getSeqp());
    }

    @Override
    public LeadResponseDTO manuallyAssign(Long leadId, Long userId, String reason) {
        Lead lead = leadsHelper.getLeadById(leadId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        if (!user.getOrgId().equals(lead.getOrgId())) {
            throw new ConflictException("That user does not belong to this lead's organization");
        }

        Long previousAssignee = lead.getAssignedToUserId();
        lead.setAssignedToUserId(user.getSeqp());
        lead.setAssignmentReason(reason);
        Lead saved = leadRepository.save(lead);
        auditLogService.record("Lead", leadId, "MANUALLY_ASSIGNED", previousAssignee, user.getSeqp());

        return leadMapper.toResponse(saved);
    }

    private boolean withinCapacity(User user) {
        if (user.getMaxConcurrentAssignments() == null) return true;
        return openLeadCount(user) < user.getMaxConcurrentAssignments();
    }

    private long openLeadCount(User user) {
        return leadRepository.countByAssignedToUserIdAndStatusNotIn(user.getSeqp(), LeadStatus.TERMINAL);
    }
}
