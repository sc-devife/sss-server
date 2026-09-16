package com.sss.app.dto.library.hotel;

import lombok.Data;

// The Subject line is the only part of the "Send Hotel Booking Email" popup
// the user can edit before sending (see HotelBookingEmailPreviewDTO) — the
// body is always server-regenerated from current data, never taken from the
// client, so a request here can't be used to smuggle arbitrary HTML into an
// outbound email.
@Data
public class HotelBookingEmailSendRequestDTO {

    private String subject;
}
