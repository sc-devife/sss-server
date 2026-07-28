package com.sss.app.repository.reminder;

import com.sss.app.entity.reminder.PaymentReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PaymentReminderLogRepository extends JpaRepository<PaymentReminderLog, Long> {

    boolean existsByMilestone_SeqpAndSentDate(Long milestoneSeqp, LocalDate sentDate);
}
