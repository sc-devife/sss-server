package com.sss.app.entity.library.hotel;

import com.sss.app.entity.library.roomtype.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

// One (Hotel, RoomType) pairing with its own price/night — replaces the old
// plain hotel_room_types join table now that price needs to vary per hotel
// for the same shared RoomType master-data row (e.g. "Deluxe Room" is
// ₹8,000/night at one hotel, ₹6,500 at another). Structurally the same idea
// as ItineraryItemHotelDetail carrying its own price against a RoomType
// reference, just at the master-data/catalog level instead of per-booking.
@Entity
@Table(name = "hotel_room_types")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelRoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;
}
