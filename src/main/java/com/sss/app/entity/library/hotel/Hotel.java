package com.sss.app.entity.library.hotel;

import com.sss.app.entity.library.destination.Destination;
import com.sss.app.entity.library.location.Location;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.roomtype.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "hotels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(nullable = false)
    private String name;

    private Integer stars;

    // ----- Location: ManyToOne (Hotel belongs to one Location) -----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // ----- Trip Destinations: ManyToMany -----
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_destinations",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "destination_id")
    )
    @Builder.Default
    private Set<Destination> destinations = new HashSet<>();

    // ----- Meal Plans: ManyToMany -----
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_meal_plans",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "meal_plan_id")
    )
    @Builder.Default
    private Set<MealPlan> mealPlans = new HashSet<>();

    // ----- Room Types: ManyToMany (shared master data) -----
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

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
