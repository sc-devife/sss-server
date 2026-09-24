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

    // Computed by the DB from seqp (see V85 migration) — human-readable
    // quote code for download filenames, e.g. "QUOTE-00001". Mirrors
    // Escape.tripCode; never app-generated so it can never drift from seqp.
    @Column(name = "quote_code", insertable = false, updatable = false)
    private String quoteCode;

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

    @Column(name = "subtotal_inr", precision = 14, scale = 2)
    private BigDecimal subtotalInr;

    @Column(name = "tax_profile_id")
    private UUID taxProfileId;

    @Column(name = "tax_amount_inr", precision = 14, scale = 2)
    private BigDecimal taxAmountInr;

    // Overrides taxProfileId's own ratePercent at compute time when set —
    // lets a quote use a one-off tax % (or a tweak to the selected profile's
    // rate) without needing a new library TaxProfile row for it. taxProfileId
    // is still recorded alongside it, so "which tax TYPE" is never lost.
    @Column(name = "tax_rate_percent_override", precision = 6, scale = 3)
    private BigDecimal taxRatePercentOverride;

    // TCS (Tax Collected at Source) — a second statutory tax on Indian
    // outbound travel packages, stacked alongside GST rather than replacing
    // it. Computed on (subtotal + GST), matching how TCS is actually levied
    // on the customer-facing package price.
    @Column(name = "tcs_rate_percent", precision = 5, scale = 2)
    private BigDecimal tcsRatePercent;

    @Column(name = "tcs_amount_inr", precision = 14, scale = 2)
    private BigDecimal tcsAmountInr;

    // When the quotation PDF was last generated, and a fingerprint of its inputs then.
    @Column(name = "generated_at")
    private java.time.LocalDateTime generatedAt;

    @Column(name = "generated_fingerprint", length = 64)
    private String generatedFingerprint;

    @Column(name = "total_inr", precision = 14, scale = 2)
    private BigDecimal totalInr;

    // Sum of dropped hotels' cancellation charges across this itinerary —
    // already folded into subtotalInr/totalInr above (see
    // QuoteComputationServiceImpl's resolvePrice), kept as its own column
    // purely so it can be shown as a distinct line (Quotation, billing)
    // rather than hidden inside the general hotel total.
    @Column(name = "cancellation_charges_inr", precision = 14, scale = 2)
    private BigDecimal cancellationChargesInr;

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
