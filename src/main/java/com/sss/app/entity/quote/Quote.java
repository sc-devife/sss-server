package com.sss.app.entity.quote;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.itinerary.Itinerary;
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

/**
 * Structural only this phase — pricing fields (subtotal/tax/total/fx) are
 * plain caller-set values, not computed. Phase 5's Quotation Engine owns
 * the actual tax-profile lookup + FX conversion + rate-snapshot logic.
 */
@Entity
@Table(name = "quotes")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    private String name;

    @Column(nullable = false)
    private Integer version;

    // draft / sent / accepted / rejected / superseded
    @Column(nullable = false)
    private String status;

    @Column(name = "currency_code")
    private String currencyCode;

    @Column(name = "fx_rate_snapshot", precision = 18, scale = 6)
    private BigDecimal fxRateSnapshot;

    @Column(name = "subtotal_usd", precision = 14, scale = 2)
    private BigDecimal subtotalUsd;

    @Column(name = "tax_profile_id")
    private UUID taxProfileId;

    @Column(name = "tax_amount_usd", precision = 14, scale = 2)
    private BigDecimal taxAmountUsd;

    @Column(name = "total_usd", precision = 14, scale = 2)
    private BigDecimal totalUsd;

    // none / percent / flat
    @Column(name = "discount_type", nullable = false)
    private String discountType;

    @Column(name = "discount_value", precision = 14, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
        if (this.status == null) {
            this.status = "draft";
        }
        if (this.version == null) {
            this.version = 1;
        }
        if (this.discountType == null) {
            this.discountType = "none";
        }
    }
}
