package com.sss.app.repository.taxprofile;

import com.sss.app.entity.taxprofile.TaxProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxProfileRepository extends JpaRepository<TaxProfile, Long> {

    Optional<TaxProfile> findByUid(UUID uid);

    List<TaxProfile> findAllByOrgId(Long orgId);
}
