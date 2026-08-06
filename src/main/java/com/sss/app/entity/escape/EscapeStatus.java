package com.sss.app.entity.escape;

import java.util.List;

/** Section 8 escape lifecycle — a strict forward-only sequence, plus Cancelled which is only reachable pre-Ongoing. */
public final class EscapeStatus {
    private EscapeStatus() {}

    public static final String PLANNING = "Planning";
    public static final String ITINERARY_DRAFTING = "Itinerary Drafting";
    public static final String QUOTATION_SENT = "Quotation Sent";
    public static final String QUOTE_ACCEPTED = "Quote Accepted"; // "(Deal)" per spec — Deal entity itself is Phase 6
    public static final String PAYMENT_PENDING = "Payment Pending";
    public static final String PARTIALLY_PAID = "Partially Paid";
    public static final String FULLY_PAID = "Fully Paid";
    public static final String ESCAPE_CONFIRMED = "Escape Confirmed";
    public static final String ONGOING = "Ongoing";
    public static final String COMPLETED = "Completed";
    public static final String CANCELLED = "Cancelled";

    public static final List<String> ORDER = List.of(
            PLANNING, ITINERARY_DRAFTING, QUOTATION_SENT, QUOTE_ACCEPTED, PAYMENT_PENDING,
            PARTIALLY_PAID, FULLY_PAID, ESCAPE_CONFIRMED, ONGOING, COMPLETED
    );

    public static int indexOf(String status) {
        return ORDER.indexOf(status);
    }
}
