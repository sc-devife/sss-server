package com.sss.app.entity.library.escapepoint;

import com.sss.app.entity.library.location.Location;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

// Which cities (Location) a destination (EscapePoint) covers — e.g. "Bali"
// covering Kuta/Ubud/Seminyak. isPrimary marks the one shown as the
// destination's headline/display city, same pattern as
// AddressConstraint.primaryAddress.
@Entity
@Table(name = "escape_point_locations")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EscapePointLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @ManyToOne
    @JoinColumn(name = "escape_point_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EscapePoint escapePoint;

    @ManyToOne
    @JoinColumn(name = "location_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Location location;

    @Builder.Default
    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static EscapePointLocation create(EscapePoint escapePoint, Location location, boolean isPrimary) {
        return EscapePointLocation.builder().escapePoint(escapePoint).location(location).isPrimary(isPrimary).build();
    }

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
