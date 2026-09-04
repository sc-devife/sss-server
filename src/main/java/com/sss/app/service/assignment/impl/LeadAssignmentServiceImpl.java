package com.sss.app.service.assignment.impl;

import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.team.Team;
import com.sss.app.entity.team.TeamStatus;
import com.sss.app.entity.team.UserTeamLink;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.team.TeamRepository;
import com.sss.app.repository.team.UserTeamLinkRepository;
import com.sss.app.service.assignment.LeadAssignmentService;
import com.sss.app.service.assignment.MetroCities;
import com.sss.app.service.assignment.PriorityCalendarService;
import com.sss.app.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Section 5 / the user's Excel writeup — priority auto-detection combines:
 * trip duration >= 4 nights, origin is a metro city, or (honeymoon/family
 * travel type + travel date falls inside an admin-configured vacation
 * season). Manual "mark as priority" override still wins outright.
 *
 * Runs once, at the moment a Lead is converted to an Escape (see
 * EscapeHelper.createEscape) — leads themselves are never individually
 * assigned.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LeadAssignmentServiceImpl implements LeadAssignmentService {

    private static final int PRIORITY_MIN_DURATION_NIGHTS = 4;
    private static final Set<String> SEASONAL_PRIORITY_TRAVEL_TYPES = Set.of("honeymoon", "family");

    private final EscapeRepository escapeRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final UserTeamLinkRepository userTeamLinkRepository;
    private final EscapeMapper escapeMapper;
    private final AuditLogService auditLogService;
    private final PriorityCalendarService priorityCalendarService;

    @Override
    public void autoAssign(Escape escape) {
        Lead lead = escape.getLead();

        if (lead != null && !Boolean.TRUE.equals(lead.getIsPriority()) && computesAsPriority(lead)) {
            lead.setIsPriority(true);
            leadRepository.save(lead);
        }

        List<User> candidates = userRepository.findUsersWithRoles(escape.getOrgId());

        boolean isPriority = lead != null && Boolean.TRUE.equals(lead.getIsPriority());
        List<User> eligible = candidates.stream()
                .filter(u -> !Boolean.FALSE.equals(u.getAcceptingLeads()))
                .filter(u -> !isPriority || Boolean.TRUE.equals(u.getEligibleForPriorityLeads()))
                .toList();

        boolean teamMatched = false;
        Set<Long> destinationSeqps = escape.getEscapePoints().stream().map(EscapePoint::getSeqp).collect(java.util.stream.Collectors.toSet());
        if (!destinationSeqps.isEmpty()) {
            List<User> teamRouted = routeByTeam(escape.getOrgId(), eligible, destinationSeqps);
            if (!teamRouted.isEmpty()) {
                eligible = teamRouted;
                teamMatched = true;
            } else {
                // Fallback: no team specializes in this destination — today's
                // individual-specialist logic, unchanged.
                List<User> specialists = eligible.stream()
                        .filter(u -> Boolean.TRUE.equals(u.getIsSpecialist())
                                && u.getSpecialistEscapePoints() != null
                                && u.getSpecialistEscapePoints().stream().anyMatch(destinationSeqps::contains))
                        .toList();
                if (!specialists.isEmpty()) {
                    eligible = specialists;
                }
            }
        }

        Optional<User> chosen = eligible.stream()
                .filter(this::withinCapacity)
                .min(Comparator.comparingLong(this::openEscapeCount));

        if (chosen.isEmpty()) {
            escapeRepository.save(escape);
            auditLogService.record("Escape", escape.getSeqp(), "AUTO_ASSIGN_SKIPPED", null,
                    "No eligible agent available — left in the unassigned queue");
            return;
        }

        User assignee = chosen.get();
        escape.setAssignedToUserId(assignee.getSeqp());
        String scopeSuffix = teamMatched ? ", routed via a matching destination team" : "";
        escape.setAssignmentReason((isPriority
                ? "Auto-assigned: priority escape, load-balanced among priority-eligible agents"
                : "Auto-assigned: round-robin load balancing") + scopeSuffix);
        escapeRepository.save(escape);
        auditLogService.record("Escape", escape.getSeqp(), "AUTO_ASSIGNED", null, assignee.getSeqp());
    }

    @Override
    public EscapeResponseDTO manuallyAssign(UUID escapeId, Long userId, String reason) {
        Escape escape = escapeRepository.findByUid(escapeId)
                .orElseThrow(() -> new NotFoundException("Escape not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        if (!user.getOrgId().equals(escape.getOrgId())) {
            throw new ConflictException("That user does not belong to this escape's organization");
        }

        Long previousAssignee = escape.getAssignedToUserId();
        escape.setAssignedToUserId(user.getSeqp());
        escape.setAssignmentReason(reason);
        Escape saved = escapeRepository.save(escape);
        auditLogService.record("Escape", escape.getSeqp(), "MANUALLY_ASSIGNED", previousAssignee, user.getSeqp());

        return escapeMapper.toResponse(saved);
    }

    // Team-first routing (User/Team metadata pass): find org teams whose
    // specializedEscapePoints overlaps the escape's destinations, narrow
    // `eligible` to those teams' active members, then — if any of those
    // members also individually specializes in the same destination — narrow
    // further to just them (team narrows the candidate pool, individual
    // specialization refines within it; both fields are kept, per that
    // decision). Returns an empty list if no team matches, signalling the
    // caller to fall back to the pre-existing individual-specialist logic.
    private List<User> routeByTeam(Long orgId, List<User> eligible, Set<Long> destinationSeqps) {
        List<Team> candidateTeams = teamRepository.findAllByOrgIdAndStatus(orgId, TeamStatus.ACTIVE).stream()
                .filter(t -> t.getSpecializedEscapePoints() != null
                        && t.getSpecializedEscapePoints().stream().anyMatch(destinationSeqps::contains))
                .toList();
        if (candidateTeams.isEmpty()) {
            return List.of();
        }

        List<Long> teamSeqps = candidateTeams.stream().map(Team::getSeqp).toList();
        Set<Long> memberSeqps = userTeamLinkRepository.findAllByTeam_SeqpIn(teamSeqps).stream()
                .filter(link -> Boolean.TRUE.equals(link.getIsActive()))
                .map(link -> link.getUser().getSeqp())
                .collect(java.util.stream.Collectors.toSet());

        List<User> teamEligible = eligible.stream().filter(u -> memberSeqps.contains(u.getSeqp())).toList();
        if (teamEligible.isEmpty()) {
            return List.of();
        }

        List<User> refined = teamEligible.stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsSpecialist())
                        && u.getSpecialistEscapePoints() != null
                        && u.getSpecialistEscapePoints().stream().anyMatch(destinationSeqps::contains))
                .toList();
        return refined.isEmpty() ? teamEligible : refined;
    }

    private boolean computesAsPriority(Lead lead) {
        if (lead.getDurationNights() != null && lead.getDurationNights() >= PRIORITY_MIN_DURATION_NIGHTS) {
            return true;
        }
        if (MetroCities.isMetro(lead.getOriginCity())) {
            return true;
        }
        if (lead.getTravelType() != null
                && SEASONAL_PRIORITY_TRAVEL_TYPES.contains(lead.getTravelType().trim().toLowerCase())
                && lead.getTravelDate() != null
                && priorityCalendarService.isDateInSeason(lead.getOrgId(), lead.getTravelDate())) {
            return true;
        }
        return false;
    }

    private boolean withinCapacity(User user) {
        if (user.getMaxConcurrentAssignments() == null) return true;
        return openEscapeCount(user) < user.getMaxConcurrentAssignments();
    }

    private long openEscapeCount(User user) {
        return escapeRepository.countByAssignedToUserIdAndStatusNotIn(user.getSeqp(), EscapeStatus.TERMINAL);
    }
}
