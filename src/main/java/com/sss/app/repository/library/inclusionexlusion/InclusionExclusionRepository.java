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

    /** Items selectable when building an itinerary: active, org-wide (unlinked) or linked to one of the trip's destinations. */
    @Query("SELECT e FROM InclusionExclusion e WHERE e.orgId = :orgId AND e.type = :type AND e.isActive = true "
            + "AND (e.destination IS NULL OR e.destination.seqp IN :destinationSeqps) "
            + "ORDER BY e.sortOrder ASC NULLS LAST, e.name ASC")
    List<InclusionExclusion> findSelectableForDestinations(@Param("orgId") Long orgId, @Param("type") String type,
                                                            @Param("destinationSeqps") List<Long> destinationSeqps);
}
