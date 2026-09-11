package com.sss.app.repository.escape;

import com.sss.app.entity.escape.Escape;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EscapeRepository extends JpaRepository<Escape, Long> {
    // Auto-cancellation sweep (AutoCancellationServiceImpl) — every escape
    // still in one of the given (pre-confirmation) statuses whose trip start
    // date is already in the past. A record only ever matches this while its
    // status stays in that list, so once it's cancelled it naturally drops
    // out of the next sweep — no separate "already processed" flag needed.
    List<Escape> findAllByStatusInAndStartDateBefore(List<String> statuses, LocalDate date);

    // "Travel date approaching" notification sweep (EscapeTravelDateReminderServiceImpl).
    List<Escape> findAllByStatusInAndStartDateAndTravelDateReminderSentAtIsNull(List<String> statuses, LocalDate date);
    @EntityGraph(attributePaths = {"lead", "travellers", "escapePoints"})
    Optional<Escape> findBySeqp(Long seqp);

    @EntityGraph(attributePaths = {"lead", "travellers", "escapePoints"})
    Optional<Escape> findByUid(UUID uid);

    @EntityGraph(attributePaths = {"lead", "travellers", "escapePoints"})
    List<Escape> findAllByOrgId(Long orgId);

    // Dashboard "My Escapes" — newest first, capped via the Pageable (e.g.
    // PageRequest.of(0, 3)) rather than fetching every assigned escape.
    @EntityGraph(attributePaths = {"lead", "travellers", "escapePoints"})
    List<Escape> findAllByOrgIdAndAssignedToUserIdAndStatusNotInOrderByCreatedAtDesc(Long orgId, Long assignedToUserId, List<String> excludedStatuses, Pageable pageable);

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
