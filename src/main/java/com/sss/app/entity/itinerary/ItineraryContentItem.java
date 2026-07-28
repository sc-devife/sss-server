package com.sss.app.entity.itinerary;

import com.sss.app.entity.common.Auditable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

/**
 * A Terms/Inclusion/Exclusion block attached to one Itinerary. Content is
 * COPIED in at attach time from an InclusionExclusion library item (or typed
 * fresh for a trip-only addition) rather than referenced live, so later
 * edits/deactivation of the library source never change an already-built
 * itinerary — same "snapshot, don't reference" pattern as Quote pricing.
 * sourceItemId is kept purely for traceability, not for live lookups.
 */
@Entity
@Table(name = "itinerary_content_items")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ItineraryContentItem extends Auditable {

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

    // INCLUSION / EXCLUSION / TERMS — see InclusionExclusionType
    @Column(nullable = false)
    private String type;

    @Column(name = "source_item_id")
    private Long sourceItemId;

    @Column(nullable = false)
    private String name;

    @Column(name = "content_html", columnDefinition = "text")
    private String contentHtml;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID();
        }
    }
}
