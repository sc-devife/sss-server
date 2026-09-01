package com.sss.app.entity.library.service;

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
 * Master data entity representing a hotel-level special service (e.g.
 * Candle Light Dinner, Room Decoration, Honeymoon Setup, Birthday
 * Decoration) — not a trip Activity. Shared/reusable across hotels,
 * many-to-many with Hotel. Structurally identical to RoomType.
 *
 * {@code hotel} is a separate concern from the {@code hotels} many-to-many
 * membership below: it marks *scope*, not selection. Null means this is
 * global master data (visible in the main Services module and selectable
 * by any hotel); set means it was created via a specific hotel's "+ Add
 * Services" and is visible only to that one hotel (and never listed in the
 * main module) — see ServiceRepository's hotel-scoped queries.
 */
@Entity
@Table(name = "services")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(nullable = false)
    private String name; // e.g. "Candle Light Dinner", "Room Decoration"

    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    // Excluded: same Hotel<->back-reference recursion risk as RoomType.hotels.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "services")
    private List<Hotel> hotels;

    // Scope marker — see class javadoc. Not the same relation as `hotels`
    // above (M:N membership); this is a direct FK owned by this table.
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
