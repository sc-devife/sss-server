package com.sss.app.repository.escape;

import com.sss.app.entity.escape.Escape;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EscapeRepository extends JpaRepository<Escape, Long> {
    @EntityGraph(attributePaths = {"lead", "travellers", "escapePoints"})
    Optional<Escape> findBySeqp(Long seqp);

    @EntityGraph(attributePaths = {"lead", "travellers", "escapePoints"})
    Optional<Escape> findByUid(UUID uid);

    @EntityGraph(attributePaths = {"lead", "travellers", "escapePoints"})
    List<Escape> findAllByOrgId(Long orgId);

    @EntityGraph(attributePaths = {"lead", "travellers", "escapePoints"})
    List<Escape> findAllByOrgIdAndAssignedToUserIdAndStatusNotIn(Long orgId, Long assignedToUserId, List<String> excludedStatuses);

    long countByOrgIdAndStatusNotIn(Long orgId, List<String> excludedStatuses);

    long countByAssignedToUserIdAndStatusNotIn(Long assignedToUserId, List<String> excludedStatuses);

    // Dashboard Escape Pipeline — one row per status actually present.
    @Query("SELECT e.status, COUNT(e) FROM Escape e WHERE e.orgId = :orgId GROUP BY e.status")
    List<Object[]> countByStatusGroupedForOrg(@Param("orgId") Long orgId);

    // Dashboard Top Escape Points — ranked by number of currently-active
    // (non-terminal) escapes touching that destination; call with a
    // Pageable (e.g. PageRequest.of(0, 10)) to cap the result.
    @Query("SELECT ep.name, COUNT(DISTINCT e) FROM Escape e JOIN e.escapePoints ep "
            + "WHERE e.orgId = :orgId AND e.status NOT IN :terminalStatuses "
            + "GROUP BY ep.name ORDER BY COUNT(DISTINCT e) DESC")
    List<Object[]> countActiveByEscapePoint(@Param("orgId") Long orgId, @Param("terminalStatuses") List<String> terminalStatuses, Pageable pageable);
}
