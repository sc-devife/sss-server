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
    private TransportDetailDTO transportDetail;
    private HotelDetailDTO hotelDetail;
}
