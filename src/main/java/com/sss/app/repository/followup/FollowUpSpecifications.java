package com.sss.app.repository.followup;

import com.sss.app.entity.followup.FollowUp;
import com.sss.app.entity.followup.FollowUpStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Composable, DB-level predicates for the /follow-ups page's Today/Yesterday/
 * Overdue/Upcoming/All filter, mirroring LeadSpecifications' shape exactly.
 */
public final class FollowUpSpecifications {
    private FollowUpSpecifications() {}

    public static Specification<FollowUp> hasOrgId(Long orgId) {
        return (root, query, cb) -> cb.equal(root.get("orgId"), orgId);
    }

    public static Specification<FollowUp> assignedToUser(Long userSeqp) {
        return (root, query, cb) -> cb.equal(root.get("assignedTo").get("seqp"), userSeqp);
    }

    public static Specification<FollowUp> matchesSearch(String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("comment")), pattern);
    }

    // Actionable + not yet Completed — the "actually still pending work"
    // subset the header count, Overdue and Upcoming filters all share.
    public static Specification<FollowUp> isActionableAndOpen() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("actionable")),
                cb.notEqual(root.get("status"), FollowUpStatus.COMPLETED));
    }

    public static Specification<FollowUp> dueToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("dueAt"), start),
                cb.lessThan(root.get("dueAt"), end));
    }

    public static Specification<FollowUp> dueYesterday() {
        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("dueAt"), start),
                cb.lessThan(root.get("dueAt"), end));
    }

    public static Specification<FollowUp> isOverdue() {
        LocalDateTime now = LocalDateTime.now();
        return (root, query, cb) -> cb.lessThan(root.get("dueAt"), now);
    }

    public static Specification<FollowUp> isUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dueAt"), now);
    }
}
