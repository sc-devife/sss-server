package com.sss.app.repository.library.inclusionexlusion;

import com.sss.app.entity.library.inclusionexclusion.InclusionExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InclusionExclusionRepository extends JpaRepository<InclusionExclusion, Long> {

    Optional<InclusionExclusion> findByUid(String uid);

    List<InclusionExclusion> findAllByOrgId(Long orgId);

    List<InclusionExclusion> findAllByOrgIdAndType(Long orgId, String type);

    boolean existsByOrgIdAndTypeAndNameIgnoreCase(Long orgId, String type, String name);

    /** Items selectable when building an itinerary: active, org-wide (unlinked) or linked to one of the escape's escape points. */
    @Query("SELECT e FROM InclusionExclusion e WHERE e.orgId = :orgId AND e.type = :type AND e.isActive = true "
            + "AND (e.escapePoint IS NULL OR e.escapePoint.seqp IN :escapePointSeqps) "
            + "ORDER BY e.sortOrder ASC NULLS LAST, e.name ASC")
    List<InclusionExclusion> findSelectableForEscapePoints(@Param("orgId") Long orgId, @Param("type") String type,
                                                            @Param("escapePointSeqps") List<Long> escapePointSeqps);
}
