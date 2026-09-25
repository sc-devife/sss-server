package com.sss.app.entity.library.activity;

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

// A payout the agency makes OUT to an activity vendor for a specific
// booking — the Activity-side counterpart of HotelPayment, scoped to one
// Activity + one Escape.
@Entity
@Table(name = "activity_payments")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityPayment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escape_id", nullable = false)
    private Escape escape;

    @Column(name = "transaction_id")
    private String transactionId;

    // upi / neft / rtgs / imps / bank_transfer / card / cash / cheque / other
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(nullable = false, precision = 16, scale = 4)
    private BigDecimal amount;

    // Set only when the payment was made in a currency other than the vendor's base (V134).
    @Column(name = "paid_amount", precision = 16, scale = 4)
    private BigDecimal paidAmount;

    @Column(name = "paid_currency")
    private String paidCurrency;

    @Column(name = "fx_rate", precision = 24, scale = 10)
    private BigDecimal fxRate;

    @Column(name = "paid_by")
    private String paidBy;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

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
