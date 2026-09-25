package com.sss.app.entity.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** One payment received against a milestone, in the currency it was actually received in (see V133). */
@Entity
@Table(name = "payment_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false, insertable = false)
    private UUID uid;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "milestone_id", nullable = false)
    private PaymentMilestone milestone;

    @Column(name = "received_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal receivedAmount;

    @Column(name = "received_currency", nullable = false)
    private String receivedCurrency;

    @Column(name = "fx_rate", nullable = false, precision = 24, scale = 10)
    private BigDecimal fxRate;

    @Column(name = "applied_amount_base", nullable = false, precision = 18, scale = 4)
    private BigDecimal appliedAmountBase;

    @Column(name = "base_value_received", nullable = false, precision = 18, scale = 4)
    private BigDecimal baseValueReceived;

    @Column(name = "fx_difference_base", nullable = false, precision = 18, scale = 4)
    private BigDecimal fxDifferenceBase;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "recorded_by")
    private Long recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    // Stamped when the milestone's verify step confirms this payment landed (V137).
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy;
}
