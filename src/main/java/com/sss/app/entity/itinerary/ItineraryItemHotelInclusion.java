package com.sss.app.entity.itinerary;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalTime;

// One special-inclusion row for a hotel stay (e.g. "Candle Light Dinner").
// startTime/durationMinutes describe when the service itself happens — the
// stay's own day is already known from the parent item's dayNumber.
@Entity
@Table(name = "itinerary_item_hotel_inclusions")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemHotelInclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_item_id", nullable = false)
    private ItineraryItem itineraryItem;

    @Column
    private String service;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column
    private String comments;
}
