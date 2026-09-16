package com.sss.app.dto.library.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityBookingDTO {

    private UUID itineraryItemUid;

    private UUID escapeUid;

    // Human-readable trip code (e.g. "TRP-000123") — see Escape.tripCode.
    private String tripCode;

    private String escapeStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate escapeStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate escapeEndDate;

    private String leadName;

    private Integer dayNumber;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    private String notes;

    // Initialize / Booked / Drop (see BookingStatus) — stored directly on
    // ItineraryItem.status for Activity items (no separate detail table
    // like Hotel's ItineraryItemHotelDetail).
    private String bookingStatus;

    // item.price × item.travelersCount (or the cancellation charge if
    // Dropped) — mirrors QuoteComputationServiceImpl's activity pricing
    // branch, so this figure matches what Quotation actually bills.
    private BigDecimal totalAmount;
}
