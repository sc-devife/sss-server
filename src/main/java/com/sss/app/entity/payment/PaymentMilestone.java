package com.sss.app.entity.payment;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.deal.Deal;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_milestones")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMilestone extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @Column(nullable = false)
    private String label;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "amount_base", nullable = false, precision = 16, scale = 4)
    private BigDecimal amountBase;

    @Column(name = "amount_paid_base", nullable = false, precision = 16, scale = 4)
    private BigDecimal amountPaidBase;

    // pending / partially_paid / paid / overdue
    @Column(nullable = false)
    private String status;

    @Column(name = "marked_paid_by")
    private Long markedPaidBy;

    @Column(name = "marked_paid_at")
    private LocalDateTime markedPaidAt;

    // upi / neft / rtgs / imps / bank_transfer / card / cash / cheque / other
    // — how the customer actually paid this instalment, captured at
    // record-payment time.
    @Column(name = "payment_method")
    private String paymentMethod;

    // The customer's own proof-of-payment reference — a UPI transaction ID,
    // a bank UTR number, a cheque number, etc. Free text since the format
    // varies by paymentMethod.
    @Column(name = "payment_reference")
    private String paymentReference;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
        if (this.status == null) {
            this.status = "pending";
        }
        if (this.amountPaidBase == null) {
            this.amountPaidBase = BigDecimal.ZERO;
        }
    }
}
