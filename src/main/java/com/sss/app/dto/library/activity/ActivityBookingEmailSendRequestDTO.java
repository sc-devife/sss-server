package com.sss.app.dto.library.activity;

import lombok.Data;

// The Subject line is the only part of the "Send Activity Booking Email"
// popup the user can edit before sending (see ActivityBookingEmailPreviewDTO)
// — the body is always server-regenerated from current data, never taken
// from the client. Mirrors HotelBookingEmailSendRequestDTO.
@Data
public class ActivityBookingEmailSendRequestDTO {

    private String subject;
}
