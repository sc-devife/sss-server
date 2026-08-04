package com.sss.app.repository.lead;

import com.sss.app.entity.lead.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    // Example: find leads by status
    List<Lead> findByStatus(String status);

    // Example: find leads by destination (for travel use case)
    List<Lead> findByDestinationIgnoreCase(String destination);

    List<Lead> findAllByOrgId(Long orgId);

    long countByAssignedToUserIdAndStatusNotIn(Long assignedToUserId, List<String> excludedStatuses);

    List<Lead> findAllByOrgIdAndAssignedToUserIdAndStatusNotIn(Long orgId, Long assignedToUserId, List<String> excludedStatuses);

    long countByOrgId(Long orgId);

    long countByOrgIdAndStatus(Long orgId, String status);

    long countByOrgIdAndCreatedAtAfter(Long orgId, LocalDateTime since);

    // Channel-intake dedup (Lead Source Integration) — scoped to org, never
    // cross-tenant. Email checked first by callers since it's the stronger
    // identifier; phone is the fallback.
    java.util.Optional<Lead> findFirstByOrgIdAndEmailIgnoreCase(Long orgId, String email);

    java.util.Optional<Lead> findFirstByOrgIdAndPhone(Long orgId, String phone);

    // Example: find leads by source (INSTAGRAM, WEBSITE, etc.)
   // List<Lead> findBySource(com.example.crm.entity.LeadSource source);
}
