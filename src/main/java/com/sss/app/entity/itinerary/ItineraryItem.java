package com.sss.app.entity.itinerary;

import com.sss.app.entity.common.Auditable;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "itinerary_items")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItem extends Auditable {

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

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    // hotel / activity / transport
    @Column(name = "item_type", nullable = false)
    private String itemType;

    // Polymorphic pointer at Hotel/Activity/Transport's uid, resolved at the
    // application layer based on itemType — no single DB FK is possible
    // across three different tables.
    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column
    private String notes;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }
}
