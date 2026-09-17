package com.sss.app.dto.library.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Populates the "Send Cancellation Email" popup on the Escape Planning tab
// for a Dropped Transport item — the Transport-side counterpart of
// HotelBookingEmailPreviewDTO/ActivityBookingEmailPreviewDTO. Transport has
// no library-entity detail page of its own to trigger this from (unlike
// Hotel/Activity), so this is triggered item-scoped instead of vendor-scoped
// — see ItineraryItemController.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransportCancellationEmailPreviewDTO {

    private String toEmail;

    private String subject;

    private String bodyHtml;
}
