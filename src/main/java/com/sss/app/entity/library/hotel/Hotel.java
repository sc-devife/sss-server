package com.sss.app.entity.library.hotel;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.location.Location;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.roomtype.RoomType;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "hotels")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @Column(nullable = false)
    private String name;

    private Integer stars;

    // ----- Location: ManyToOne (Hotel belongs to one Location) -----
    // Excluded from toString/equals/hashCode: Location.hotels is the inverse
    // side of this same relation, so a plain @Data on both would recurse
    // Hotel -> Location -> hotels -> Hotel -> ... until StackOverflowError
    // (the exact bug already found & fixed for User<->UserRoleLink in Phase 1).
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // ----- Escape Point: ManyToOne, per the data dictionary. Added alongside
    // the older escapePoints M2M below rather than replacing it — see V9
    // migration notes for why a clean 1:1 backfill wasn't possible for every
    // row. New code should read/write this field going forward. -----
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private EscapePoint escapePoint;

    // ----- Escape Points: ManyToMany, kept for backward compatibility with
    // pre-existing multi-escape-point hotel data (see `escapePoint` above
    // for the dictionary-aligned single-FK field). -----
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_escape_points",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "escape_point_id")
    )
    @Builder.Default
    private Set<EscapePoint> escapePoints = new HashSet<>();

    // ----- Meal Plans: ManyToMany -----
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_meal_plans",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "meal_plan_id")
    )
    @Builder.Default
    private Set<MealPlan> mealPlans = new HashSet<>();

    // ----- Room Types: ManyToMany (shared master data) -----
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_room_types",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "room_type_id")
    )
    @Builder.Default
    private Set<RoomType> roomTypes = new HashSet<>();

    // ----- Services (per the "Services" form section) -----
    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private String childAgeForExtraBed; // e.g. "Below 6 years" - kept as String to allow free-form ranges

    // Validity window for the currently-quoted rate — no rate/price field
    // exists on Hotel itself yet, but stale rates silently going out of date
    // is a real risk even before that lands, so this is worth tracking now.
    @Column(name = "rate_valid_from")
    private java.time.LocalDate rateValidFrom;

    @Column(name = "rate_valid_to")
    private java.time.LocalDate rateValidTo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    // ----- Data dictionary fields (Section 15) -----
    @Column
    private String address;

    @Column(name = "contact_info")
    private String contactInfo;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> images;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> amenities;

    @Column
    private String status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
