package com.sss.app.entity.itinerary;

import java.util.List;

// Stored as a plain String column on ItineraryItemHotelDetail (not a JPA
// @Enumerated enum) — same convention as FollowUpStatus/EscapeStatus.
public final class BookingStatus {

    private BookingStatus() {
    }

    public static final String INITIALIZE = "Initialize";
    public static final String BOOKED = "Booked";
    public static final String DROP = "Drop";

    public static final List<String> ALL = List.of(INITIALIZE, BOOKED, DROP);
}
