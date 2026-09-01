package com.sss.app.entity.itinerary;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// One leg of a transport booking — one_way has 1, round_trip has 2
// (onward/return), multi_city has N, ordered by legOrder. Only meaningful
// for flight today, but not type-gated so any mode could use it later.
@Entity
@Table(name = "itinerary_item_transport_legs")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemTransportLeg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_item_id", nullable = false)
    private ItineraryItem itineraryItem;

    @Column(name = "leg_order", nullable = false)
    private Integer legOrder;

    // onward / return / null (multi_city legs are ordered by legOrder instead)
    @Column
    private String direction;

    @Column(name = "departure_airport")
    private String departureAirport;

    @Column(name = "departure_terminal")
    private String departureTerminal;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_airport")
    private String arrivalAirport;

    @Column(name = "arrival_terminal")
    private String arrivalTerminal;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "flight_number")
    private String flightNumber;
}
