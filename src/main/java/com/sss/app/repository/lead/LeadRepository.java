package com.sss.app.repository.lead;

import com.sss.app.entity.lead.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// JpaSpecificationExecutor backs the Leads page's combined search/status/
// date-range/pagination query (LeadSpecifications + LeadsHelper.getAllLeads)
// — one dynamic query built at the DB level instead of fetching everything
// and filtering in Java.
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {

    Optional<Lead> findByUid(UUID uid);

    // Auto-cancellation sweep (AutoCancellationServiceImpl) — every lead
    // still in one of the given (non-terminal) statuses whose travel date is
    // already in the past. Mirrors EscapeRepository's equivalent — a record
    // only matches while its status stays in that list, so once it's marked
    // lost it naturally drops out of the next sweep.
    List<Lead> findAllByStatusInAndTravelDateBefore(List<String> statuses, LocalDate date);

    // Example: find leads by status
    List<Lead> findByStatus(String status);

    // Example: find leads by destination (for travel use case)
    List<Lead> findByDestinationIgnoreCase(String destination);

    long countByOrgId(Long orgId);

    long countByOrgIdAndStatus(Long orgId, String status);

    long countByOrgIdAndCreatedAtAfter(Long orgId, LocalDateTime since);

    // For the "New Leads vs previous period" trend arrow (Dashboard).
    long countByOrgIdAndCreatedAtBetween(Long orgId, LocalDateTime start, LocalDateTime end);

    // Dashboard Lead Funnel — one row per status actually present.
    @Query("SELECT l.status, COUNT(l) FROM Lead l WHERE l.orgId = :orgId GROUP BY l.status")
    List<Object[]> countByStatusGroupedForOrg(@Param("orgId") Long orgId);

    // Dashboard Lead Source donut — Agency leads have no sourceChannel, so
    // they're bucketed by sourceType instead; a genuinely unset channel on a
    // direct lead falls back to "Unknown" rather than being silently dropped.
    @Query("SELECT CASE WHEN l.sourceType = 'AGENCY' THEN 'Agency' ELSE COALESCE(l.sourceChannel, 'Unknown') END, COUNT(l) "
            + "FROM Lead l WHERE l.orgId = :orgId "
            + "GROUP BY CASE WHEN l.sourceType = 'AGENCY' THEN 'Agency' ELSE COALESCE(l.sourceChannel, 'Unknown') END")
    List<Object[]> countBySourceGroupedForOrg(@Param("orgId") Long orgId);

    // Dashboard Leads Trend — native query for date_trunc, since JPQL has no
    // portable equivalent. `granularity` is either "day" or "month", passed
    // in from the service after validating the requested period.
    @Query(value = "SELECT date_trunc(:granularity, created_at) AS bucket, COUNT(*) AS cnt "
            + "FROM leads WHERE org_id = :orgId AND created_at >= :since "
            + "GROUP BY bucket ORDER BY bucket", nativeQuery = true)
    List<Object[]> countTrendForOrg(@Param("orgId") Long orgId, @Param("granularity") String granularity, @Param("since") LocalDateTime since);

    // Channel-intake dedup (Lead Source Integration) — scoped to org, never
    // cross-tenant. Email checked first by callers since it's the stronger
    // identifier; phone is the fallback.
    java.util.Optional<Lead> findFirstByOrgIdAndEmailIgnoreCase(Long orgId, String email);

    java.util.Optional<Lead> findFirstByOrgIdAndPhone(Long orgId, String phone);

    // Example: find leads by source (INSTAGRAM, WEBSITE, etc.)
   // List<Lead> findBySource(com.example.crm.entity.LeadSource source);
}
