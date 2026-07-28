package com.sss.app.entity.library.inclusionexclusion;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Reusable Terms &amp; Conditions / Inclusion / Exclusion content blocks
 * (Section 8's itinerary Terms/Inclusions/Exclusions). Optionally linked to
 * one Destination (EscapePoint); left unlinked, an item lives in the
 * org-wide library and can still be attached to any itinerary. Attaching to
 * an itinerary copies the content into ItineraryContentItem rather than
 * referencing this row live.
 */
@Entity
@Table(name = "inclusion_exclusion_items")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class InclusionExclusion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(insertable = false, updatable = false)
    private String uid;

    private Long orgId;

    @Column(nullable = false)
    private String name;

    // INCLUSION / EXCLUSION / TERMS — see InclusionExclusionType
    @Column(nullable = false)
    private String type;

    @Column(name = "content_html", columnDefinition = "text")
    private String contentHtml;

    private Long sortOrder;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private EscapePoint destination;
}
