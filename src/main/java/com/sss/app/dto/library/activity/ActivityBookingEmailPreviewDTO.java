package com.sss.app.dto.library.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Populates the "Send Activity Booking Email" popup on the Activity Detail
// page's Bookings tab before anything is actually sent — toEmail is null
// when the activity has no email on file (the frontend gates opening the
// popup on this same Activity.email check, so toEmail here is mostly a
// defensive echo). Mirrors HotelBookingEmailPreviewDTO.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityBookingEmailPreviewDTO {

    private String toEmail;

    private String subject;

    private String bodyHtml;
}
