package com.sss.app.service.integration.impl;

import com.sss.app.service.integration.LeadChannelAdapter;
import com.sss.app.service.integration.NormalizedLeadPayload;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * The one real, wired-end-to-end channel for v1 (Section 7 recommends
 * shipping a generic inbound webhook first). Tolerant of a few common key
 * spellings since external senders won't agree on field names — matches the
 * kind of per-channel field mapping the user's own requirements doc shows
 * for Facebook/WhatsApp leads (name/number/email/travel_date + custom
 * fields), just generalized rather than hardcoded to one vendor's schema.
 */
@Component
public class WebhookLeadAdapter implements LeadChannelAdapter {

    @Override
    public String channelCode() {
        return "webhook";
    }

    @Override
    public NormalizedLeadPayload normalize(Map<String, Object> rawPayload) {
        NormalizedLeadPayload payload = new NormalizedLeadPayload();
        payload.setName(firstNonBlank(rawPayload, "name", "customer_name", "full_name"));
        payload.setEmail(firstNonBlank(rawPayload, "email", "cust_email"));
        payload.setPhone(firstNonBlank(rawPayload, "phone", "number", "customer_number", "mobile"));
        payload.setDestinationHint(firstNonBlank(rawPayload, "destination", "destination_hint"));
        payload.setSourceRefId(firstNonBlank(rawPayload, "source_ref_id", "lead_id", "id"));
        payload.setTravelDate(parseDate(firstNonBlank(rawPayload, "travel_date", "travelDate")));
        payload.setNumberOfPeople(parseInt(firstNonBlank(rawPayload, "number_of_people", "pax", "travellers")));
        payload.setDurationNights(resolveDurationNights(rawPayload));
        payload.setRawPayload(rawPayload);
        return payload;
    }

    // "nights" and "duration_days"/"duration" are NOT interchangeable —
    // a sender using the "nights" key already means the same thing this
    // adapter stores, so it's taken as-is; a sender using "duration_days"/
    // "duration" means a day count, which is one more than the nights we
    // store, so it needs the "-1" conversion. Preferring an explicit
    // "nights" key over the day-count aliases when both happen to be present
    // avoids double-guessing a sender that already speaks our units.
    private Integer resolveDurationNights(Map<String, Object> rawPayload) {
        Integer nights = parseInt(firstNonBlank(rawPayload, "nights"));
        if (nights != null) {
            return nights;
        }
        Integer days = parseInt(firstNonBlank(rawPayload, "duration_days", "duration"));
        return days != null ? days - 1 : null;
    }

    private String firstNonBlank(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private LocalDate parseDate(String value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
