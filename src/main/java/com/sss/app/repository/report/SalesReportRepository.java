package com.sss.app.repository.report;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grouped aggregates behind the Sales reports. Every report is the same cohort —
 * the org's non-archived leads created in [start, end) — grouped three
 * different ways; only the group expression (and the join it needs) differs.
 * Leads, Completed/Hold/Cancelled and Revenue are each one grouped query, not
 * a query per row.
 */
@Repository
public class SalesReportRepository {

    /** How a report groups its leads. */
    public enum Grouping {
        // Group key is the assigned user's seqp (null = unassigned).
        SALES_PERSON("", "l.assignedToUserId"),
        // A lead can cover several escape points, so it counts under each of them.
        ESCAPE_POINT("JOIN l.escapePoints ep", "ep.seqp"),
        // Same bucketing as the Dashboard's Lead Source donut: Agency leads by
        // type, Direct leads by channel, an unset channel as "Unknown".
        LEAD_SOURCE("", "CASE WHEN l.sourceType = 'AGENCY' THEN 'Agency' ELSE COALESCE(l.sourceChannel, 'Unknown') END");

        final String join;
        final String expr;

        Grouping(String join, String expr) {
            this.join = join;
            this.expr = expr;
        }
    }

    // Cohort filter shared by all three queries (alias `l` = Lead).
    private static final String COHORT =
            "l.orgId = :orgId AND l.deletedAt IS NULL AND l.createdAt >= :start AND l.createdAt < :end";

    @PersistenceContext
    private EntityManager em;

    /** [groupKey, leadCount] */
    public Map<Object, Long> countLeads(Grouping g, Long orgId, LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = em.createQuery(
                        "SELECT " + g.expr + ", COUNT(DISTINCT l.seqp) FROM Lead l " + g.join
                                + " WHERE " + COHORT + " GROUP BY " + g.expr, Object[].class)
                .setParameter("orgId", orgId).setParameter("start", start).setParameter("end", end)
                .getResultList();
        Map<Object, Long> out = new HashMap<>();
        for (Object[] r : rows) out.put(r[0], ((Number) r[1]).longValue());
        return out;
    }

    /** [groupKey, escapeStatus] -> count of the cohort's escapes in that status. */
    public Map<Object, Map<String, Long>> countEscapesByStatus(Grouping g, Long orgId, LocalDateTime start,
                                                                LocalDateTime end, List<String> statuses) {
        List<Object[]> rows = em.createQuery(
                        "SELECT " + g.expr + ", e.status, COUNT(DISTINCT e.seqp) FROM Escape e JOIN e.lead l " + g.join
                                + " WHERE " + COHORT + " AND e.orgId = :orgId AND e.status IN :statuses"
                                + " GROUP BY " + g.expr + ", e.status", Object[].class)
                .setParameter("orgId", orgId).setParameter("start", start).setParameter("end", end)
                .setParameter("statuses", statuses)
                .getResultList();
        Map<Object, Map<String, Long>> out = new HashMap<>();
        for (Object[] r : rows) {
            out.computeIfAbsent(r[0], k -> new HashMap<>()).put((String) r[1], ((Number) r[2]).longValue());
        }
        return out;
    }

    /**
     * Revenue per group — the same definition the Dashboard uses for
     * totalRevenueInr: the sum of accepted quotes' totalInr.
     */
    public Map<Object, BigDecimal> sumAcceptedQuoteRevenue(Grouping g, Long orgId, LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = em.createQuery(
                        "SELECT " + g.expr + ", COALESCE(SUM(q.totalInr), 0) FROM Quote q JOIN q.itinerary i JOIN i.escape e"
                                + " JOIN e.lead l " + g.join
                                + " WHERE " + COHORT + " AND q.orgId = :orgId AND q.status = 'accepted'"
                                + " GROUP BY " + g.expr, Object[].class)
                .setParameter("orgId", orgId).setParameter("start", start).setParameter("end", end)
                .getResultList();
        Map<Object, BigDecimal> out = new HashMap<>();
        for (Object[] r : rows) out.put(r[0], r[1] == null ? BigDecimal.ZERO : new BigDecimal(r[1].toString()));
        return out;
    }
}
