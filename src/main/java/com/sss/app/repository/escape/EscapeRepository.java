package com.sss.app.repository.escape;

import com.sss.app.entity.escape.Escape;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EscapeRepository extends JpaRepository<Escape, Long> {
    @EntityGraph(attributePaths = {"lead", "travellers", "destinations"})
    Optional<Escape> findBySeqp(Long seqp);

    @EntityGraph(attributePaths = {"lead", "travellers", "destinations"})
    List<Escape> findAllByOrgId(Long orgId);

    @EntityGraph(attributePaths = {"lead", "travellers", "destinations"})
    List<Escape> findAllByOrgIdAndLead_AssignedToUserIdAndStatusNotIn(Long orgId, Long assignedToUserId, List<String> excludedStatuses);

    long countByOrgIdAndStatusNotIn(Long orgId, List<String> excludedStatuses);
}
