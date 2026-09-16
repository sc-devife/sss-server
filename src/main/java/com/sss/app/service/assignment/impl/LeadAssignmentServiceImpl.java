package com.sss.app.service.assignment.impl;

import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.lead.LeadStatus;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.team.Team;
import com.sss.app.entity.team.TeamStatus;
import com.sss.app.entity.team.UserTeamLink;
import com.sss.app.entity.users.User;
import com.sss.app.entity.notification.NotificationType;
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
import com.sss.app.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Section 5 / Assignment Rules — priority auto-detection combines: trip
 * duration >= 4 nights, origin is a metro city, or (honeymoon/family travel
 * type + travel date falls inside an admin-configured vacation season).
 * Manual "mark as priority" override still wins outright.
 *
 * Candidate selection is a weighted-scoring model, not a chain of hard
 * filters: only "accepting leads" and "has capacity right now" ever exclude
 * an agent outright (see selectAssignee) — priority/specialist/large-group/
 * language are all scored preferences, so a Lead is never left unassigned
 * just because nobody happens to match every preference. Destination *team*
 * routing (routeByTeam) is the one exception that still hard-narrows the
 * pool: it's a distinct, more specific org-configured signal (a team
 * explicitly owns a destination), not one of the per-agent Assignment Rules
 * fields this scoring model covers.
 *
 * Runs at two points:
 * <ul>
 *   <li>{@link #autoAssignLead}, once, at Lead intake (see
 *       LeadsHelper.createLead and the channel-intake variants) — routes the
 *       Lead itself to an agent before it's ever converted.</li>
 *   <li>{@link #autoAssign}, once, when a Lead is converted to an Escape
 *       (see EscapeHelper.createEscape) — if the source Lead already carries
 *       its own assignment, that's carried over as-is; otherwise the engine
 *       scores fresh here (the pre-Lead-routing behavior, still the fallback
 *       for leads created before this existed or with auto-assign off at
 *       intake).</li>
 * </ul>
 *
 * If no eligible candidate is found (nobody accepting leads, or everybody at
 * capacity), the Lead/Escape is left unassigned with a specific reason
 * recorded — a Lead Assigner picks it up manually via manuallyAssignLead /
 * manuallyAssign.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LeadAssignmentServiceImpl implements LeadAssignmentService {

    private static final int PRIORITY_MIN_DURATION_NIGHTS = 4;
    private static final Set<String> SEASONAL_PRIORITY_TRAVEL_TYPES = Set.of("honeymoon", "family");
    private static final int LARGE_GROUP_MIN_TRAVELERS = 5;

    // Weighted-scoring model per the Assignment Rules spec. Specialist/
    // language give a base score for the first match plus a smaller bonus
    // per additional match, so an agent covering more of the Lead's escape
    // points/languages consistently outranks one covering fewer.
    private static final int PRIORITY_MATCH_SCORE = 30;
    private static final int SPECIALIST_BASE_SCORE = 30;
    private static final int SPECIALIST_ADDITIONAL_MATCH_SCORE = 10;
    private static final int LARGE_GROUP_MATCH_SCORE = 20;
    private static final int LANGUAGE_BASE_SCORE = 20;
    private static final int LANGUAGE_ADDITIONAL_MATCH_SCORE = 5;

    private final EscapeRepository escapeRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final UserTeamLinkRepository userTeamLinkRepository;
    private final EscapeMapper escapeMapper;
    private final AuditLogService auditLogService;
    private final PriorityCalendarService priorityCalendarService;
    private final NotificationService notificationService;

    @Override
    public void autoAssignLead(Lead lead) {
        if (!Boolean.TRUE.equals(lead.getIsPriority()) && computesAsPriority(lead)) {
            lead.setIsPriority(true);
        }

        boolean isPriority = Boolean.TRUE.equals(lead.getIsPriority());
        AssignmentCriteria criteria = new AssignmentCriteria(
                lead.getEscapePoints(), isPriority, isLargeGroup(lead.getNumberOfPeople()), lead.getLanguages());
        Optional<AssigneeSelection> picked = selectAssignee(lead.getOrgId(), criteria);

        if (picked.isEmpty()) {
            leadRepository.save(lead);
            String reason = noEligibleAgentReason(lead.getOrgId());
            auditLogService.record("Lead", lead.getSeqp(), "AUTO_ASSIGN_SKIPPED", null, reason);
            log.info("Lead {} left unassigned — {}", lead.getUid(), reason);
            return;
        }

        AssigneeSelection selection = picked.get();
        User assignee = selection.user();
        lead.setAssignedToUserId(assignee.getSeqp());
        lead.setAssignmentReason(selection.describeReason());
        leadRepository.save(lead);
        auditLogService.record("Lead", lead.getSeqp(), "AUTO_ASSIGNED", null, assignee.getSeqp());
        log.info("Lead {} auto-assigned to {} — {}", lead.getUid(), assignee.getEmail(), selection.describeReason());

        notificationService.notify(assignee.getSeqp(), lead.getOrgId(),
                NotificationType.LEAD_ASSIGNED, "Lead Assigned",
                "A Lead has been assigned to you.",
                NotificationType.RelatedEntityType.LEAD, lead.getUid());
    }

    @Override
    public void autoAssign(Escape escape) {
        Lead lead = escape.getLead();

        if (lead != null && !Boolean.TRUE.equals(lead.getIsPriority()) && computesAsPriority(lead)) {
            lead.setIsPriority(true);
            leadRepository.save(lead);
        }

        // The Lead may already carry its own assignment from intake-time
        // routing (autoAssignLead, above) — if so, the same agent keeps the
        // deal through conversion instead of the engine potentially picking
        // someone different here.
        if (lead != null && lead.getAssignedToUserId() != null) {
            escape.setAssignedToUserId(lead.getAssignedToUserId());
            escape.setAssignmentReason("Carried over from the Lead's own assignment");
            escapeRepository.save(escape);
            auditLogService.record("Escape", escape.getSeqp(), "AUTO_ASSIGNED", null, lead.getAssignedToUserId());
            notificationService.notify(lead.getAssignedToUserId(), escape.getOrgId(),
                    NotificationType.ESCAPE_ASSIGNED, "Escape Assigned",
                    "An Escape has been assigned to you.",
                    NotificationType.RelatedEntityType.ESCAPE, escape.getUid());
            return;
        }

        boolean isPriority = lead != null && Boolean.TRUE.equals(lead.getIsPriority());
        Integer partySize = lead != null ? lead.getNumberOfPeople() : escape.getTravellers().size();
        List<String> languages = lead != null ? lead.getLanguages() : null;
        AssignmentCriteria criteria = new AssignmentCriteria(
                escape.getEscapePoints(), isPriority, isLargeGroup(partySize), languages);
        Optional<AssigneeSelection> picked = selectAssignee(escape.getOrgId(), criteria);

        if (picked.isEmpty()) {
            escapeRepository.save(escape);
            String reason = noEligibleAgentReason(escape.getOrgId());
            auditLogService.record("Escape", escape.getSeqp(), "AUTO_ASSIGN_SKIPPED", null, reason);
            log.info("Escape {} left unassigned — {}", escape.getUid(), reason);
            return;
        }

        AssigneeSelection selection = picked.get();
        User assignee = selection.user();
        escape.setAssignedToUserId(assignee.getSeqp());
        escape.setAssignmentReason(selection.describeReason());
        escapeRepository.save(escape);
        auditLogService.record("Escape", escape.getSeqp(), "AUTO_ASSIGNED", null, assignee.getSeqp());
        log.info("Escape {} auto-assigned to {} — {}", escape.getUid(), assignee.getEmail(), selection.describeReason());

        notificationService.notify(assignee.getSeqp(), escape.getOrgId(),
                NotificationType.ESCAPE_ASSIGNED, "Escape Assigned",
                "An Escape has been assigned to you.",
                NotificationType.RelatedEntityType.ESCAPE, escape.getUid());
    }

    @Override
    public Lead manuallyAssignLead(UUID leadId, Long userId, String reason) {
        Lead lead = leadRepository.findByUid(leadId)
                .orElseThrow(() -> new NotFoundException("Lead not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        if (!user.getOrgId().equals(lead.getOrgId())) {
            throw new ConflictException("That user does not belong to this lead's organization");
        }

        Long previousAssignee = lead.getAssignedToUserId();
        lead.setAssignedToUserId(user.getSeqp());
        lead.setAssignmentReason(reason);
        Lead saved = leadRepository.save(lead);
        auditLogService.record("Lead", lead.getSeqp(), "MANUALLY_ASSIGNED", previousAssignee, user.getSeqp());

        notificationService.notify(user.getSeqp(), saved.getOrgId(),
                NotificationType.LEAD_ASSIGNED, "Lead Assigned",
                "A Lead has been assigned to you.",
                NotificationType.RelatedEntityType.LEAD, saved.getUid());

        return saved;
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

        notificationService.notify(user.getSeqp(), saved.getOrgId(),
                NotificationType.ESCAPE_ASSIGNED, "Escape Assigned",
                "An Escape has been assigned to you.",
                NotificationType.RelatedEntityType.ESCAPE, saved.getUid());

        return escapeMapper.toResponse(saved);
    }

    // Everything selectAssignee needs to know about the Lead/Escape being
    // routed — deliberately Lead/Escape-agnostic so one scoring path serves
    // both autoAssignLead and autoAssign.
    private record AssignmentCriteria(Set<EscapePoint> destinations, boolean isPriority, boolean isLargeGroup,
                                       List<String> languages) {
    }

    // A candidate mid-ranking, before the capacity lock/recheck.
    private record ScoredCandidate(User user, int score, List<String> matchedCriteria, long openWorkload) {
    }

    private record AssigneeSelection(User user, boolean teamMatched, int score, List<String> matchedCriteria,
                                      long activeWorkload, Integer capacity) {
        // Section 13 audit format — a compact, single-line rendering of "why
        // this agent" for the assignmentReason field/audit log/application
        // log, e.g. "Auto-assigned: Priority Lead, Bali Specialist, Hindi
        // (3/10 active) — score 80".
        String describeReason() {
            String matched = matchedCriteria.isEmpty() ? "round-robin load balancing" : String.join(", ", matchedCriteria);
            String scopeSuffix = teamMatched ? ", routed via a matching destination team" : "";
            String capacitySuffix = capacity != null ? " (" + activeWorkload + "/" + capacity + " active)"
                    : " (" + activeWorkload + " active)";
            return "Auto-assigned: " + matched + scopeSuffix + capacitySuffix + " — score " + score;
        }
    }

    // Shared candidate-selection core for both autoAssignLead and autoAssign.
    // Hard eligibility is just "accepting leads" + team-destination routing;
    // everything else (priority/specialist/large-group/language) is scored,
    // then candidates are walked highest-score-first, taking a row lock on
    // each in turn and re-checking capacity under that lock — see
    // UserRepository.lockForAssignment. That recheck is what makes this safe
    // against two Leads created in the same instant both landing on an agent
    // who only has room for one: whichever transaction locks the row first
    // sees a stale-safe count, and the second sees the first's committed
    // update once the lock releases.
    private Optional<AssigneeSelection> selectAssignee(Long orgId, AssignmentCriteria criteria) {
        List<User> candidates = userRepository.findUsersWithRoles(orgId);

        List<User> accepting = candidates.stream()
                .filter(u -> !Boolean.FALSE.equals(u.getAcceptingLeads()))
                .toList();

        Set<Long> destinationSeqps = criteria.destinations().stream().map(EscapePoint::getSeqp).collect(Collectors.toSet());
        boolean teamMatched = false;
        List<User> pool = accepting;
        if (!destinationSeqps.isEmpty()) {
            List<User> teamRouted = routeByTeam(orgId, accepting, destinationSeqps);
            if (!teamRouted.isEmpty()) {
                pool = teamRouted;
                teamMatched = true;
            }
        }

        boolean finalTeamMatched = teamMatched;
        List<ScoredCandidate> ranked = pool.stream()
                .map(u -> score(u, criteria))
                .sorted(Comparator.comparingInt(ScoredCandidate::score).reversed()
                        .thenComparingLong(ScoredCandidate::openWorkload)
                        .thenComparing(c -> c.user().getSeqp()))
                .toList();

        for (ScoredCandidate candidate : ranked) {
            User locked = userRepository.lockForAssignment(candidate.user().getSeqp()).orElse(null);
            if (locked == null || !withinCapacity(locked)) {
                // Lost the race (or somehow vanished) — move on to the next
                // best-ranked candidate rather than giving up outright.
                continue;
            }
            return Optional.of(new AssigneeSelection(locked, finalTeamMatched, candidate.score(),
                    candidate.matchedCriteria(), openWorkloadCount(locked), locked.getMaxConcurrentAssignments()));
        }
        return Optional.empty();
    }

    // Section 7 scoring: priority/specialist/large-group/language are each a
    // preference, never a hard requirement — an agent matching none of them
    // still scores 0 and remains selectable (falls through to workload
    // balancing among the "accepting + within capacity" pool).
    private ScoredCandidate score(User user, AssignmentCriteria criteria) {
        int score = 0;
        List<String> matched = new ArrayList<>();

        if (criteria.isPriority() && Boolean.TRUE.equals(user.getEligibleForPriorityLeads())) {
            score += PRIORITY_MATCH_SCORE;
            matched.add("Priority Lead");
        }

        int specialistMatches = 0;
        if (Boolean.TRUE.equals(user.getIsSpecialist()) && user.getSpecialistEscapePoints() != null) {
            for (EscapePoint destination : criteria.destinations()) {
                if (user.getSpecialistEscapePoints().contains(destination.getSeqp())) {
                    specialistMatches++;
                    matched.add(destination.getName() + " Specialist");
                }
            }
        }
        if (specialistMatches > 0) {
            score += SPECIALIST_BASE_SCORE + (specialistMatches - 1) * SPECIALIST_ADDITIONAL_MATCH_SCORE;
        }

        if (criteria.isLargeGroup() && Boolean.TRUE.equals(user.getEligibleForLargeGroups())) {
            score += LARGE_GROUP_MATCH_SCORE;
            matched.add(">5 Travelers");
        }

        int languageMatches = 0;
        if (criteria.languages() != null && user.getLanguages() != null) {
            for (String language : criteria.languages()) {
                if (user.getLanguages().contains(language)) {
                    languageMatches++;
                    matched.add(language);
                }
            }
        }
        if (languageMatches > 0) {
            score += LANGUAGE_BASE_SCORE + (languageMatches - 1) * LANGUAGE_ADDITIONAL_MATCH_SCORE;
        }

        return new ScoredCandidate(user, score, matched, openWorkloadCount(user));
    }

    // Section 12 fallback-level reasoning, computed only on the rare no-match
    // path — distinguishes *why* nobody was eligible so the audit/log entry
    // is actionable instead of a generic "no one available".
    private String noEligibleAgentReason(Long orgId) {
        List<User> candidates = userRepository.findUsersWithRoles(orgId);
        if (candidates.isEmpty()) {
            return "No agents exist in this organization";
        }
        List<User> accepting = candidates.stream().filter(u -> !Boolean.FALSE.equals(u.getAcceptingLeads())).toList();
        if (accepting.isEmpty()) {
            return "All agents have Accepting Leads turned off";
        }
        if (accepting.stream().noneMatch(this::withinCapacity)) {
            return "All eligible agents reached their concurrent lead capacity";
        }
        return "No eligible agent available — left in the unassigned queue";
    }

    // Section 4 addition: leads/escapes carrying more than 5 travellers score
    // higher against agents who've opted into handling large parties (see
    // score()) rather than being excluded outright.
    private boolean isLargeGroup(Integer numberOfPeople) {
        return numberOfPeople != null && numberOfPeople > LARGE_GROUP_MIN_TRAVELERS;
    }

    // Team-first routing (User/Team metadata pass): find org teams whose
    // specializedEscapePoints overlaps the escape's destinations, narrow
    // `eligible` to those teams' active members, then — if any of those
    // members also individually specializes in the same destination — narrow
    // further to just them (team narrows the candidate pool, individual
    // specialization refines within it; both fields are kept, per that
    // decision). Returns an empty list if no team matches, signalling the
    // caller to keep the broader (non-team-narrowed) pool.
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
                .collect(Collectors.toSet());

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
        return openWorkloadCount(user) < user.getMaxConcurrentAssignments();
    }

    // "Open work" spans both open Escapes and open (unconverted, unresolved)
    // Leads — the per-agent settings panel has always labelled this cap "Max
    // concurrent leads/escapes".
    private long openWorkloadCount(User user) {
        return escapeRepository.countByAssignedToUserIdAndStatusNotIn(user.getSeqp(), EscapeStatus.TERMINAL)
                + leadRepository.countByAssignedToUserIdAndStatusNotIn(user.getSeqp(), LeadStatus.TERMINAL);
    }
}
