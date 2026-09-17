package com.sss.app.dto.library.transport;

import lombok.Data;

// The Subject line is the only editable part of the "Send Cancellation
// Email" popup — see HotelBookingEmailSendRequestDTO's own comment for why
// the body is never taken from the client.
@Data
public class TransportCancellationEmailSendRequestDTO {

    private String subject;
}
