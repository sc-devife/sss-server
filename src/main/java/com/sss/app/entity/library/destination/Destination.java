package com.sss.app.entity.library.destination;

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
 * Master data entity representing a Trip Destination / tour region.
 * Many-to-many with Hotel - a Hotel can serve multiple trip destinations
 * and a Destination can include many Hotels.
 */
@Entity
@Table(name = "destinations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "destinations")
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
