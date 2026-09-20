package com.sss.app.entity.library.hotel;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.location.Location;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.service.Service;
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
    // @BatchSize here (and on the three collections below) turns "one lazy
    // SELECT per hotel per collection" into "one SELECT per batch of hotels
    // per collection" whenever a list of hotels is in the persistence context
    // together (e.g. getAll()'s findAllByOrgIdAndDeletedAtIsNull) — this is
    // what fixes the N+1 that made GET /api/v1/hotels take 20-70s with 500+
    // rows, without changing what any single hotel's data looks like.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_escape_points",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "escape_point_id")
    )
    @Builder.Default
    @org.hibernate.annotations.BatchSize(size = 50)
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
    @org.hibernate.annotations.BatchSize(size = 50)
    private Set<MealPlan> mealPlans = new HashSet<>();

    // ----- Room Types: OneToMany to the join entity, not a plain ManyToMany
    // — each (Hotel, RoomType) pairing carries its own price/night, since the
    // same shared RoomType master-data row ("Deluxe Room") can be priced
    // differently at different hotels. See HotelRoomType. orphanRemoval so
    // clearing/re-adding this collection (the update pattern used whenever
    // the whole pricing list is resubmitted) deletes the dropped rows. -----
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @org.hibernate.annotations.BatchSize(size = 50)
    private Set<HotelRoomType> roomTypes = new HashSet<>();

    // ----- Services: ManyToMany — hotel-level special add-ons (Candle Light
    // Dinner, Room Decoration, Honeymoon Setup, Birthday Decoration), not
    // trip Activities (trekking/scuba/etc, see the Activity entity for that). -----
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_services",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    @Builder.Default
    @org.hibernate.annotations.BatchSize(size = 50)
    private Set<Service> services = new HashSet<>();

    // ----- Services (per the "Services" form section) -----
    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private String childAgeForExtraBed; // e.g. "Below 6 years" - kept as String to allow free-form ranges

    // Starting/indicative rate — shown in the itinerary's hotel suggestion
    // dropdown alongside stars. Not the price actually booked at (that's
    // ItineraryItemHotelDetail.price, entered per-stay); this is the
    // library-level "from" rate, same role Transport.basePrice/
    // Activity.basePrice play for their own suggestion dropdowns.
    @Column(name = "base_price", precision = 12, scale = 2)
    private java.math.BigDecimal basePrice;

    // Validity window for the currently-quoted rate above.
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

    // Legacy free-text field — superseded by the structured phoneNumber/
    // email pair below for the Add/Edit Hotel form, kept only for existing
    // data and bulk import's own "contactInfo" CSV column.
    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> images;

    // The manually-chosen main image for this Hotel — must always be one of
    // `images` (enforced in HotelServiceImpl), never inferred from array
    // order. Mirrors EscapePoint.priorityImage.
    @Column(name = "priority_image")
    private String priorityImage;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> amenities;

    @Column
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String about;

    // HTML from the RichTextEditor on the Add/Edit Hotel form.
    @Column(name = "rules_and_policies", columnDefinition = "TEXT")
    private String rulesAndPolicies;

    // ----- Account tab: this hotel's own payout details (bank account or
    // UPI), used when the agency settles a booking with the hotel directly —
    // unrelated to the org-wide OrganizationBankDetails, which is the
    // agency's own receivable account, not a vendor's. All optional; a
    // hotel may be paid by bank transfer, UPI, or both. -----
    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "branch_name")
    private String branchName;

    // "Savings" or "Current" — plain string, not an enum (same free-form
    // choice as Hotel.status elsewhere on this entity).
    @Column(name = "account_type", length = 20)
    private String accountType;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(length = 20)
    private String ifsc;

    @Column(name = "upi_id")
    private String upiId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
