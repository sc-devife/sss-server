package com.sss.app.repository.integration.meta;

import com.sss.app.entity.integration.meta.LeadSourceMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeadSourceMetadataRepository extends JpaRepository<LeadSourceMetadata, Long> {

    Optional<LeadSourceMetadata> findByLeadId(Long leadId);
}
