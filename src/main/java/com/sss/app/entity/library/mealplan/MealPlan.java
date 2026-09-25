package com.sss.app.entity.library.mealplan;

import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Master data entity representing a Meal Plan (e.g. CP, MAP, AP, EP).
 * Many-to-many with Hotel.
 */
@Entity
@Table(name = "meal_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(nullable = false)
    private String code; // e.g. "CP", "MAP", "AP", "EP"

    @Column(nullable = false)
    private String name; // e.g. "Continental Plan"

    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    // Excluded: same Hotel<->back-reference recursion risk as Location.hotels.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "mealPlans")
    private List<Hotel> hotels;

    // Scope marker: null = global library meal plan; set = a custom plan created for
    // (and only visible to) that one hotel. Mirrors Service.hotel.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
