package com.sss.app.entity.library.location;

import com.sss.app.entity.library.hotel.Hotel;
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
 * Master data entity representing a Hotel's city/state location.
 * One Location can have many Hotels (OneToMany).
 */
@Entity
@Table(name = "locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @Column(nullable = false)
    private String city;

    private String state;

    private String country;

    @Column(nullable = false, unique = true)
    private String displayName; // e.g. "Goa, Goa, India" - used for filters/search matching

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    // Ground-truth geography for whatever sits here (Hotel, Activity, Transport, ServiceProvider) —
    // used for pricing/tax/scheduling, unlike the escape point's representative currency/timezone.
    private String currencyCode;

    private String timeZone;

    // Excluded: Hotel.location -> Location.hotels -> Hotel -> ... would
    // otherwise recurse indefinitely (see Hotel.java's note on this pattern).
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "location")
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
