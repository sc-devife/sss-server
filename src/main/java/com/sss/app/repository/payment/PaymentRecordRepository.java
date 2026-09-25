package com.sss.app.repository.payment;

import com.sss.app.entity.payment.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    List<PaymentRecord> findAllByMilestone_SeqpOrderByRecordedAtAsc(Long milestoneSeqp);

    List<PaymentRecord> findAllByOrgId(Long orgId);
}
