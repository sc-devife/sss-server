package com.sss.app.dto.library.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelBookingDTO {

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

    // Hotel-stay lifecycle status (Initialize / Booked / Drop) — see
    // BookingStatus, stored on ItineraryItemHotelDetail.status. Null when the
    // item's hotel detail form has never been filled in.
    private String bookingStatus;

    private String mealPlanName;

    // Free-text special-inclusion names (ItineraryItemHotelInclusion.service)
    // booked alongside this stay — not a subset of Hotel.services.
    private List<String> services;

    // Stay price (or cancellation charge if Dropped) plus the sum of all
    // inclusion totalPrices — mirrors QuoteComputationServiceImpl's hotel
    // pricing so this figure always matches what Quotation actually bills.
    private BigDecimal totalAmount;
}
