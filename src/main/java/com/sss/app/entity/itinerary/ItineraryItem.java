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

import java.time.LocalTime;
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

    // transport / pickup_drop / hotel / activity / sightseeing / meal / free_time / other
    @Column(name = "item_type", nullable = false)
    private String itemType;

    // Polymorphic pointer at Hotel/Activity/Transport/ServiceProvider's uid
    // depending on itemType, resolved at the application layer — no single
    // DB FK is possible across four different tables. Optional: an ad-hoc
    // entry with no matching library record yet supplies only `title`. See
    // ItineraryItemHelper.validateReference for the "title OR referenceId"
    // rule.
    @Column(name = "reference_id")
    private UUID referenceId;

    // "library" when referenceId points at a Hotel/Activity/Transport/
    // ServiceProvider record, "custom" for a free-text-only entry — derived
    // server-side from referenceId's presence (see ItineraryItemHelper), not
    // client-settable, so it can never drift from what referenceId actually says.
    @Column(nullable = false)
    private String source;

    // Free-text label — required when referenceId is absent, optional
    // (pre-filled from the library item's name, user-overridable) otherwise.
    @Column
    private String title;

    @Column(name = "start_time")
    private LocalTime startTime;

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
        if (this.source == null) {
            this.source = referenceId != null ? "library" : "custom";
        }
    }
}
