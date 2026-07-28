package com.sss.app.repository.payment;

import com.sss.app.entity.payment.PaymentMilestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMilestoneRepository extends JpaRepository<PaymentMilestone, Long> {

    Optional<PaymentMilestone> findByUid(UUID uid);

    List<PaymentMilestone> findAllByOrgIdAndDeal_SeqpOrderByDueDateAsc(Long orgId, Long dealSeqp);

    List<PaymentMilestone> findAllByDeal_Seqp(Long dealSeqp);

    // Used by the reminder job: every not-yet-fully-paid milestone, across all orgs.
    List<PaymentMilestone> findAllByStatusIn(List<String> statuses);
}
