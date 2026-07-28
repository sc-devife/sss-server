package com.sss.app.entity.reminder;

import com.sss.app.entity.payment.PaymentMilestone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "payment_reminder_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id", nullable = false)
    private PaymentMilestone milestone;

    @Column(name = "sent_date", nullable = false)
    private LocalDate sentDate;
}
