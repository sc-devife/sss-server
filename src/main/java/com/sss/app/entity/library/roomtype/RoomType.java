package com.sss.app.entity.library.roomtype;

import com.sss.app.entity.library.hotel.Hotel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Master data entity representing a Room Type (e.g. Deluxe Room, Suite).
 * Shared/reusable across hotels - many-to-many with Hotel.
 */
@Entity
@Table(name = "room_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(nullable = false, unique = true)
    private String name; // e.g. "Deluxe Room", "Suite"

    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "roomTypes")
    private List<Hotel> hotels;

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
