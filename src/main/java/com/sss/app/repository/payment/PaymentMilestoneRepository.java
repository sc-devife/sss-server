package com.sss.app.repository.payment;

import com.sss.app.entity.payment.PaymentMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMilestoneRepository extends JpaRepository<PaymentMilestone, Long> {

    Optional<PaymentMilestone> findByUid(UUID uid);

    List<PaymentMilestone> findAllByOrgIdAndDeal_SeqpOrderByDueDateAsc(Long orgId, Long dealSeqp);

    List<PaymentMilestone> findAllByDeal_Seqp(Long dealSeqp);

    // Used by the reminder job: every not-yet-fully-paid milestone, across all orgs.
    List<PaymentMilestone> findAllByStatusIn(List<String> statuses);

    // Dashboard org metrics: outstanding amount across all open milestones for an org.
    List<PaymentMilestone> findAllByOrgIdAndStatusIn(Long orgId, List<String> statuses);

    // Dashboard "my upcoming payments": milestones on escapes assigned to me.
    @Query("SELECT pm FROM PaymentMilestone pm WHERE pm.orgId = :orgId AND pm.deal.escape.assignedToUserId = :userId "
            + "AND pm.status IN :statuses ORDER BY pm.dueDate ASC")
    List<PaymentMilestone> findUpcomingForAssignee(@Param("orgId") Long orgId, @Param("userId") Long userId, @Param("statuses") List<String> statuses);

    // Dashboard Revenue & Payments — one row per status actually present,
    // with both billed (amountUsd) and collected (amountPaidUsd) totals so
    // the same query backs the donut, the KPI totals, and the overdue card.
    @Query("SELECT pm.status, COUNT(pm), SUM(pm.amountUsd), SUM(pm.amountPaidUsd) "
            + "FROM PaymentMilestone pm WHERE pm.orgId = :orgId GROUP BY pm.status")
    List<Object[]> aggregateByStatusForOrg(@Param("orgId") Long orgId);

    // Dashboard "Revenue Collected vs previous period" trend arrow.
    @Query("SELECT COALESCE(SUM(pm.amountPaidUsd), 0) FROM PaymentMilestone pm "
            + "WHERE pm.orgId = :orgId AND pm.markedPaidAt BETWEEN :start AND :end")
    BigDecimal sumPaidBetween(@Param("orgId") Long orgId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
