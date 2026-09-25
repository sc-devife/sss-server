package com.sss.app.entity.quote;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One itinerary item's row inside a Quote's line-item breakdown — see
 * QuoteComputationServiceImpl.syncLineItems for how these are kept in sync
 * with the itinerary's actual items, and V128's migration comment for why
 * this table exists (per-item discount on top of the quote's own overall
 * discount).
 */
@Entity
@Table(name = "quote_line_items")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteLineItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_item_id", nullable = false)
    private ItineraryItem itineraryItem;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(nullable = false)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_cancellation", nullable = false)
    @Builder.Default
    private Boolean isCancellation = false;

    @Column(name = "gross_amount_base", nullable = false, precision = 14, scale = 4)
    private BigDecimal grossAmountBase;

    // none / percent / flat
    @Column(name = "discount_type", nullable = false)
    private String discountType;

    @Column(name = "discount_value", precision = 14, scale = 4)
    private BigDecimal discountValue;

    @Column(name = "net_amount_base", nullable = false, precision = 14, scale = 4)
    private BigDecimal netAmountBase;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
        if (this.discountType == null) {
            this.discountType = "none";
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }
}
