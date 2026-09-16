package com.sss.app.dto.library.hotel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Populates the "Send Hotel Booking Email" popup on the Hotel Detail page's
// Bookings tab before anything is actually sent — toEmail is null when the
// hotel has no email on file (the frontend gates opening the popup on this
// same Hotel.email check, so toEmail here is mostly a defensive echo).
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelBookingEmailPreviewDTO {

    private String toEmail;

    private String subject;

    private String bodyHtml;
}
