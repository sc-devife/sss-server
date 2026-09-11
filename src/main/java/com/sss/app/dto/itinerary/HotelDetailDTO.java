package com.sss.app.dto.itinerary;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class HotelDetailDTO {

    private UUID mealPlanId;

    private UUID roomTypeId;

    // Consecutive nights this stay covers, starting from the item's own
    // dayNumber (check-in day). See ItineraryItemHotelDetail.nights.
    private Integer nights;

    private Integer paxPerRoom;

    private Integer roomCount;

    private Integer adultsWithExtraBed;

    private Integer childrenWithExtraBed;

    private Integer childrenNoBed;

    private Integer complimentaryChildCount;

    private BigDecimal price;

    private BigDecimal totalPrice;

    private List<HotelInclusionDTO> inclusions;

    // Initialize / Booked / Drop — see BookingStatus. Ignored by the
    // server on the very first save of a hotel's detail (always forced to
    // Initialize then); only meaningful on an update.
    private String status;

    // Required when status is being changed to Drop; ignored otherwise.
    private String droppingReason;

    // Optional cancellation/drop charge from the hotel — only meaningful
    // when status is Drop.
    private BigDecimal cancellationCharge;
}
