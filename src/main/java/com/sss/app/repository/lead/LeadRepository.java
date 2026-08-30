package com.sss.app.repository.lead;

import com.sss.app.entity.lead.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByUid(UUID uid);

    // Example: find leads by status
    List<Lead> findByStatus(String status);

    // Example: find leads by destination (for travel use case)
    List<Lead> findByDestinationIgnoreCase(String destination);

    List<Lead> findAllByOrgId(Long orgId);


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
