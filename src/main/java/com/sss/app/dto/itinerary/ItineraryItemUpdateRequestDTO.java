package com.sss.app.dto.itinerary;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class ItineraryItemUpdateRequestDTO {
    private Integer dayNumber;
    private String itemType;
    private UUID referenceId;
    private String title;
    private LocalTime startTime;
    private String notes;
    private String longDescription;
    private BigDecimal price;

    // Currently only meaningful for itemType "activity" — see
    // ItineraryItem.travelersCount.
    private Integer travelersCount;

    private TransportDetailDTO transportDetail;
    private HotelDetailDTO hotelDetail;

    // Item-level Initialize/Booked/Drop status — for item types with no
    // dedicated detail table of their own (currently Activity; Hotel keeps
    // its own separate status on HotelDetailDTO). droppingReason is
    // required when status is being changed to Drop; cancellationCharge is
    // optional and only meaningful then. See ItineraryItemHelper.update.
    private String status;
    private String droppingReason;
    private BigDecimal cancellationCharge;
}
