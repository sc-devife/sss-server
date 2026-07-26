package com.sss.app.repository.library.inclusionexlusion;

import com.sss.app.entity.library.inclusionexclusion.InclusionExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface InclusionExclusionRepository extends JpaRepository<InclusionExclusion, Long> {
    @Query("SELECT e FROM InclusionExclusion e WHERE e.orgId = :orgId")
    List<InclusionExclusion> findInclusionExclusionsByOrgId(@Param("orgId") Long orgId);

    @Query("SELECT e FROM InclusionExclusion e WHERE e.orgId = :orgId and e.type = 'INCLUSION'")
    List<InclusionExclusion> findInclusionsByOrgId(@Param("orgId") Long orgId);

    @Query("SELECT e FROM InclusionExclusion e WHERE e.orgId = :orgId and e.type = 'EXCLUSION'")
    List<InclusionExclusion> findExclusionsByOrgId(@Param("orgId") Long orgId);

    Optional<InclusionExclusion> findByUid(String uid);

    boolean existsByName(String name);
}
