package com.sss.app.dto.itinerary;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class ItineraryItemResponseDTO {
    private UUID uid;
    private UUID itineraryUid;
    private Integer dayNumber;
    private String itemType;
    private UUID referenceId;
    private String referenceLabel; // resolved at response-build time (library name, or title if ad-hoc)
    private String source; // "library" or "custom" — see ItineraryItem.source
    private String title;
    private LocalTime startTime;
    private String notes;
    private String longDescription;
    private BigDecimal price;
    private Integer travelersCount;
    private Integer sortOrder;
    private TransportDetailDTO transportDetail;
    private HotelDetailDTO hotelDetail;

    // Item-level Initialize/Booked/Drop status — see
    // ItineraryItemUpdateRequestDTO's own comment.
    private String status;
    private String droppingReason;
    private BigDecimal cancellationCharge;

    // Set only on a hotel item created by the Change/Replace Hotel flow —
    // the (now Dropped) hotel item it replaces. See ItineraryItem.replacesItem.
    private UUID replacesItemUid;
}
