package com.sss.app.repository.library.inclusionexlusion;

import com.sss.app.entity.library.inclusionexclusion.InclusionExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface InclusionExclusionRepository extends JpaRepository<InclusionExclusion, Long> {
    @Query("SELECT e FROM InclusionExclusion e WHERE e.company_id = :companyId")
    List<InclusionExclusion> findInclusionExclusionsByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT e FROM InclusionExclusion e WHERE e.company_id = :companyId and e.type = 'INCLUSION'")
    List<InclusionExclusion> findInclusionsByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT e FROM InclusionExclusion e WHERE e.company_id = :companyId and e.type = 'EXCLUSION'")
    List<InclusionExclusion> findExclusionsByCompanyId(@Param("companyId") Long companyId);

    Optional<InclusionExclusion> findByUid(String uid);

    boolean existsByName(String name);
}
