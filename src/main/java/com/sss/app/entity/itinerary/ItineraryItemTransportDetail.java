package com.sss.app.entity.itinerary;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

// Booking-specific transport detail — not reusable master data like the
// library Transport entity, since a flight number/date/time only ever
// applies to one trip. 1:1 with ItineraryItem, only present once a
// transport item's detail form has actually been filled in.
@Entity
@Table(name = "itinerary_item_transport_details")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemTransportDetail {

    @Id
    @Column(name = "itinerary_item_id")
    private Long itineraryItemId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "itinerary_item_id")
    private ItineraryItem itineraryItem;

    // car/coach/flight/train/boat/etc — mirrors Transport.modeCode, needed
    // here too since a custom (non-library) transport entry has no Transport
    // record to read it from.
    @Column(name = "mode_code")
    private String modeCode;

    // Mirrors Transport.vehicleTypeCode; doubles as "Travel Class" for flight.
    @Column(name = "vehicle_type_code")
    private String vehicleTypeCode;

    // Simple flat price — used by every non-flight mode.
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    // one_way / round_trip / multi_city — flight only.
    @Column(name = "trip_type")
    private String tripType;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "cost_price_per_person")
    private Boolean costPricePerPerson;

    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "selling_price_per_person")
    private Boolean sellingPricePerPerson;

    @Column(name = "adults_count")
    private Integer adultsCount;

    @Column(name = "children_count")
    private Integer childrenCount;

    @Column(name = "infants_count")
    private Integer infantsCount;

    // Free text — the reference screenshot's single "Baggage / Fare / Meal" field.
    @Column(name = "additional_options")
    private String additionalOptions;
}
