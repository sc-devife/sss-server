package com.sss.app.repository.lead;

import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Composable, DB-level predicates for the Leads page's combined
 * search/status/date-range/pagination query (LeadsHelper.getAllLeads) —
 * each filter is only "and"-ed in when actually present, so any
 * combination (or none) produces one clean query instead of fetching
 * everything and filtering in Java.
 */
public final class LeadSpecifications {
    private LeadSpecifications() {}

    public static Specification<Lead> hasOrgId(Long orgId) {
        return (root, query, cb) -> cb.equal(root.get("orgId"), orgId);
    }

    public static Specification<Lead> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    // Excludes archived leads (LeadLifecycleService.archive) from the
    // default Leads list — always "and"-ed in by LeadsHelper.getAllLeads.
    public static Specification<Lead> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    // The Leads page's Archive checkbox, checked: swaps the default
    // notDeleted() filter for this one, showing only archived leads instead
    // of "and"-ing them together (an exclusive toggle, same shape as
    // isPriority() above).
    public static Specification<Lead> isArchived() {
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    // Leads page's new Escape Point toolbar filter — joins the same
    // lead_escape_points table the entity's escapePoints field already maps,
    // matching by EscapePoint.uid (a String, not the Lead's own UUID uid).
    public static Specification<Lead> hasEscapePoint(String escapePointUid) {
        return (root, query, cb) -> {
            Join<Lead, EscapePoint> join = root.join("escapePoints", JoinType.INNER);
            return cb.equal(join.get("uid"), escapePointUid);
        };
    }

    // More Filters' Source multi-select — "agency" matches sourceType=AGENCY
    // (Agency leads carry no sourceChannel); every other value matches
    // sourceChannel directly (only ever set on DIRECT leads).
    public static Specification<Lead> matchesSources(List<String> sources) {
        return (root, query, cb) -> {
            var predicates = sources.stream()
                    .map(s -> "agency".equalsIgnoreCase(s)
                            ? cb.equal(root.get("sourceType"), "AGENCY")
                            : cb.equal(root.get("sourceChannel"), s))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new);
            return cb.or(predicates);
        };
    }

    // The Leads page's Status dropdown also offers a "Priority" pseudo-status
    // (LeadsPanel.tsx's STATUS_FILTER_OPTIONS) backed by this separate
    // boolean column, not the `status` field itself.
    public static Specification<Lead> isPriority() {
        return (root, query, cb) -> cb.isTrue(root.get("isPriority"));
    }

    // Mirrors the frontend's previous client-side search exactly (LeadsPanel/
    // DataTable's name column filterValue combined name + email + phone into
    // one searchable string) — now the same three fields, OR'd, at the DB level.
    public static Specification<Lead> matchesSearch(String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("phone")), pattern));
    }

    // `start` inclusive, `end` exclusive — callers pass the instant just past
    // the period's last moment, so the last day of a range is always fully
    // included with no 23:59:59-precision guessing.
    public static Specification<Lead> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("createdAt"), start),
                cb.lessThan(root.get("createdAt"), end));
    }
}
