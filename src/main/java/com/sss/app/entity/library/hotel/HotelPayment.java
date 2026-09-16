package com.sss.app.entity.library.hotel;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.escape.Escape;
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
import java.util.UUID;

// A payout the agency makes OUT to a hotel for a specific booking — the
// counterpart of PaymentMilestone (money collected FROM a customer for a
// Deal), which this is deliberately NOT tied to: this is a vendor payable,
// scoped to one Hotel + one Escape, not a Deal's receivable schedule.
@Entity
@Table(name = "hotel_payments")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelPayment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escape_id", nullable = false)
    private Escape escape;

    @Column(name = "transaction_id")
    private String transactionId;

    // upi / neft / rtgs / imps / bank_transfer / card / cash / cheque / other
    // — free string, same convention as PaymentMilestone.paymentMethod.
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    // Free-text name of who disbursed the payment — no existing User/
    // Traveller reference in this codebase models this concept, so it stays
    // a plain string rather than inventing a new relation for it.
    @Column(name = "paid_by")
    private String paidBy;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Always "Paid" today — this records an already-completed payout, not a
    // multi-step receivable like PaymentMilestone's pending/verify flow.
    @Builder.Default
    @Column(nullable = false)
    private String status = "Paid";

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
