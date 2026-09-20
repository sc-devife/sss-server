package com.sss.app.helper.followup;

import com.sss.app.dto.followup.FollowUpCreateRequestDTO;
import com.sss.app.dto.followup.FollowUpUpdateRequestDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.followup.FollowUp;
import com.sss.app.entity.followup.FollowUpStatus;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.notification.NotificationType;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.followup.FollowUpRepository;
import com.sss.app.repository.followup.FollowUpSpecifications;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FollowUpsHelper {

    private final FollowUpRepository followUpRepository;
    private final LeadRepository leadRepository;
    private final EscapeRepository escapeRepository;
    private final UserRepository userRepository;
    private final OrgAccessGuard orgAccessGuard;
    private final NotificationService notificationService;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public FollowUp getFollowUpById(UUID uid) {
        FollowUp followUp = followUpRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Follow-up not found with id: " + uid));
        orgAccessGuard.requireAccessToOrg(followUp.getOrgId());
        return followUp;
    }

    public FollowUp createFollowUp(FollowUpCreateRequestDTO request) {
        if ((request.getLeadUid() == null) == (request.getEscapeUid() == null)) {
            throw new BadRequestException("A follow-up must be attached to exactly one of Lead or Escape");
        }

        User creator = currentUser();
        boolean actionable = request.getActionable() == null || request.getActionable();
        if (actionable && request.getDueAt() == null) {
            throw new BadRequestException("Due date/time is required for an actionable follow-up");
        }

        FollowUp.FollowUpBuilder builder = FollowUp.builder()
                .orgId(creator.getOrgId())
                .comment(request.getComment())
                .actionable(actionable)
                .dueAt(actionable ? request.getDueAt() : null)
                .status(FollowUpStatus.PENDING)
                .assignedTo(resolveAssignee(request.getAssignedToUid(), creator));

        if (request.getLeadUid() != null) {
            Lead lead = leadRepository.findByUid(request.getLeadUid())
                    .orElseThrow(() -> new NotFoundException("Lead not found"));
            orgAccessGuard.requireAccessToOrg(lead.getOrgId());
            builder.lead(lead);
        } else {
            Escape escape = escapeRepository.findByUid(request.getEscapeUid())
                    .orElseThrow(() -> new NotFoundException("Escape not found"));
            orgAccessGuard.requireAccessToOrg(escape.getOrgId());
            builder.escape(escape);
        }

        FollowUp saved = followUpRepository.save(builder.build());
        notifyAssignment(saved);
        return saved;
    }

    // Actor-skip is handled inside NotificationService.notify itself — this
    // fires unconditionally, and simply becomes a no-op when the assignee is
    // whoever just created/edited the follow-up.
    private void notifyAssignment(FollowUp followUp) {
        notificationService.notify(followUp.getAssignedTo().getSeqp(), followUp.getOrgId(),
                NotificationType.FOLLOWUP_ASSIGNED, "Follow-up Assigned",
                "\"" + followUp.getComment() + "\" has been assigned to you.",
                NotificationType.RelatedEntityType.FOLLOWUP, followUp.getUid());
    }

    public FollowUp updateFollowUp(UUID uid, FollowUpUpdateRequestDTO request) {
        FollowUp followUp = getFollowUpById(uid);
        boolean actionable = request.getActionable() == null || request.getActionable();
        if (actionable && request.getDueAt() == null) {
            throw new BadRequestException("Due date/time is required for an actionable follow-up");
        }

        Long previousAssigneeSeqp = followUp.getAssignedTo() != null ? followUp.getAssignedTo().getSeqp() : null;
        followUp.setComment(request.getComment());
        followUp.setActionable(actionable);
        followUp.setDueAt(actionable ? request.getDueAt() : null);
        followUp.setAssignedTo(resolveAssignee(request.getAssignedToUid(), currentUser()));
        FollowUp saved = followUpRepository.save(followUp);

        // Only notify when the assignment actually changed — an unrelated
        // comment/due-date edit on an already-assigned follow-up shouldn't
        // re-notify the same assignee every time.
        if (!saved.getAssignedTo().getSeqp().equals(previousAssigneeSeqp)) {
            notifyAssignment(saved);
        }
        return saved;
    }

    // Not a field edit — the table's inline Status dropdown. Sets/clears
    // completedAt so "removed from Overdue" (the flow diagram's last step)
    // falls straight out of isActionableAndOpen() with no extra bookkeeping.
    public FollowUp updateStatus(UUID uid, String status) {
        if (!FollowUpStatus.ALL.contains(status)) {
            throw new BadRequestException("Status must be one of: " + FollowUpStatus.ALL);
        }
        FollowUp followUp = getFollowUpById(uid);
        followUp.setStatus(status);
        followUp.setCompletedAt(FollowUpStatus.COMPLETED.equals(status) ? LocalDateTime.now() : null);
        FollowUp saved = followUpRepository.save(followUp);

        // "completed by another user" — notify.notify() already no-ops when
        // the recipient is the current actor, so completing your own
        // follow-up is silently a no-op here without an extra check.
        if (FollowUpStatus.COMPLETED.equals(status)) {
            notificationService.notify(saved.getAssignedTo().getSeqp(), saved.getOrgId(),
                    NotificationType.FOLLOWUP_COMPLETED, "Follow-up Completed",
                    "\"" + saved.getComment() + "\" has been marked complete.",
                    NotificationType.RelatedEntityType.FOLLOWUP, saved.getUid());
        }
        return saved;
    }

    // The /follow-ups page's own list — always scoped to the caller, never
    // another user's or another org's follow-ups.
    // `from`/`to` (a Day picked as from == to, or a Between range) take over
    // from the quick `filter` when either is given.
    public Page<FollowUp> getAllForCurrentUser(String filter, String search, LocalDate from, LocalDate to, Pageable pageable) {
        User user = currentUser();
        Specification<FollowUp> spec = Specification
                .where(FollowUpSpecifications.hasOrgId(user.getOrgId()))
                .and(FollowUpSpecifications.assignedToUser(user.getSeqp()));

        if (from != null || to != null) {
            spec = spec.and(FollowUpSpecifications.dueBetween(from, to));
        } else {
            spec = switch (filter == null ? "today" : filter.toLowerCase()) {
                case "yesterday" -> spec.and(FollowUpSpecifications.dueYesterday());
                case "overdue" -> spec.and(FollowUpSpecifications.isActionableAndOpen()).and(FollowUpSpecifications.isOverdue());
                case "upcoming" -> spec.and(FollowUpSpecifications.isActionableAndOpen()).and(FollowUpSpecifications.isUpcoming());
                case "all" -> spec;
                default -> spec.and(FollowUpSpecifications.dueToday());
            };
        }

        if (search != null && !search.isBlank()) {
            spec = spec.and(FollowUpSpecifications.matchesSearch(search.trim()));
        }
        return followUpRepository.findAll(spec, pageable);
    }

    // Lead/Escape detail page's "Tasks & Comments" section — every follow-up
    // tied to that record, any assignee (a shared team view, not filtered to
    // the caller), org-guarded via the parent record itself.
    public List<FollowUp> getAllForLead(UUID leadUid) {
        Lead lead = leadRepository.findByUid(leadUid).orElseThrow(() -> new NotFoundException("Lead not found"));
        orgAccessGuard.requireAccessToOrg(lead.getOrgId());
        return followUpRepository.findAllByLead_SeqpOrderByCreatedAtDesc(lead.getSeqp());
    }

    public List<FollowUp> getAllForEscape(UUID escapeUid) {
        Escape escape = escapeRepository.findByUid(escapeUid).orElseThrow(() -> new NotFoundException("Escape not found"));
        orgAccessGuard.requireAccessToOrg(escape.getOrgId());
        return followUpRepository.findAllByEscape_SeqpOrderByCreatedAtDesc(escape.getSeqp());
    }

    public long countOpenForCurrentUser() {
        User user = currentUser();
        return followUpRepository.countByAssignedTo_SeqpAndOrgIdAndActionableTrueAndStatusNot(
                user.getSeqp(), user.getOrgId(), FollowUpStatus.COMPLETED);
    }

    // Blank/null -> the current user. Otherwise resolved and verified to be
    // in the same org as the caller — "Do not allow assigning a task to a
    // user from another organization."
    private User resolveAssignee(String assignedToUid, User fallback) {
        if (assignedToUid == null || assignedToUid.isBlank()) {
            return fallback;
        }
        User assignee = userRepository.findByUid(assignedToUid)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (assignee.getOrgId() == null || !assignee.getOrgId().equals(fallback.getOrgId())) {
            throw new BadRequestException("Cannot assign a follow-up to a user from another organization");
        }
        return assignee;
    }
}
