package com.sss.app.repository.integration.meta;

import com.sss.app.entity.integration.meta.LeadImportAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeadImportAttemptRepository extends JpaRepository<LeadImportAttempt, Long> {

    Optional<LeadImportAttempt> findByUid(UUID uid);

    Optional<LeadImportAttempt> findByOrgIdAndLeadgenId(Long orgId, String leadgenId);

    Page<LeadImportAttempt> findAllByOrgIdAndProviderOrderByLastAttemptedAtDesc(Long orgId, String provider, Pageable pageable);
}
