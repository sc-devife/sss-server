package com.sss.app.entity.library.transport;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.serviceprovider.ServiceProvider;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transports")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transport extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    // single_select: car/coach/flight/train/boat (Section 15)
    @Column(name = "mode_code", nullable = false)
    private String modeCode;

    @Column(name = "vehicle_type_code")
    private String vehicleTypeCode;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column
    private Integer capacity;

    // "single" (an individual owner-operator, identified by contactName/
    // contactNumber, no provider) vs "multi" (a fleet/provider company,
    // identified by `provider`) — drives which of those the Add/Edit form
    // shows and requires, see TransportPanel.tsx.
    @Column(name = "owner_type", length = 20)
    private String ownerType;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ServiceProvider provider;

    @Column(name = "base_price", precision = 14, scale = 4)
    private BigDecimal basePrice;

    // Currency of this item's prices; null = the vendor's base currency (V136).
    @Column(name = "price_currency")
    private String priceCurrency;

    @Column(name = "pickup_location")
    private String pickupLocation;

    @Column(name = "drop_location")
    private String dropLocation;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_number", length = 30)
    private String contactNumber;

    @Column(name = "contact_email")
    private String contactEmail;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private EscapePoint escapePoint;

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
