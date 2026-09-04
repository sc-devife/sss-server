package com.sss.app.entity.itinerary;

import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.roomtype.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

// Booking-specific hotel stay detail — not reusable master data like the
// library Hotel entity, since room count/occupancy/rate only ever applies
// to one stay. 1:1 with ItineraryItem, only present once a hotel item's
// detail form has actually been filled in. Mirrors
// ItineraryItemTransportDetail's shape; mealPlan/roomType are real relations
// (not raw ids) to match how Hotel itself links to that same master data.
@Entity
@Table(name = "itinerary_item_hotel_details")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemHotelDetail {

    @Id
    @Column(name = "itinerary_item_id")
    private Long itineraryItemId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "itinerary_item_id")
    private ItineraryItem itineraryItem;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id")
    private MealPlan mealPlan;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    // How many consecutive nights this stay covers, starting from the
    // parent ItineraryItem's own day_number (its check-in day). Check-out
    // day/date is always derived (day_number + nights), never a separately
    // stored field, so it can never drift out of sync.
    @Column(nullable = false)
    @Builder.Default
    private Integer nights = 1;

    // "Pax/room (WoEB)" — base occupancy per room, without extra bed.
    @Column(name = "pax_per_room")
    private Integer paxPerRoom;

    @Column(name = "room_count")
    private Integer roomCount;

    @Column(name = "adults_with_extra_bed")
    private Integer adultsWithExtraBed;

    @Column(name = "children_with_extra_bed")
    private Integer childrenWithExtraBed;

    @Column(name = "children_no_bed")
    private Integer childrenNoBed;

    @Column(name = "complimentary_child_count")
    private Integer complimentaryChildCount;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;
}
