package com.sss.app.service.email;

import com.sss.app.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves the "To" list for a Quotation/Invoice email straight from the
 * rendered Mustache data map (QuotationDataService/BillingDataService's
 * "lead"/"travellers" entries) — the exact same Escape-lead/traveller
 * relationship already exposed to the frontend via EscapeResponseDTO, not a
 * separate address book or duplicated traveller-email lookup.
 */
@Component
public class EscapeEmailRecipientResolver {

    @SuppressWarnings("unchecked")
    public List<String> resolveFromRenderedData(Map<String, Object> data) {
        Set<String> recipients = new LinkedHashSet<>();

        Object leadObj = data.get("lead");
        if (leadObj instanceof Map<?, ?> lead) {
            addIfValid(recipients, (String) lead.get("email"));
        }

        Object travellersObj = data.get("travellers");
        if (travellersObj instanceof List<?> travellers) {
            for (Object t : travellers) {
                if (t instanceof Map<?, ?> traveller) {
                    addIfValid(recipients, (String) traveller.get("email"));
                }
            }
        }

        if (recipients.isEmpty()) {
            throw new BadRequestException("No valid email address found for this escape's lead or travellers — add an email before sending.");
        }
        return List.copyOf(recipients);
    }

    // Case-insensitive de-dup (so "A@x.com" and "a@x.com" count as one
    // recipient and are sent to only once) while keeping the first-seen
    // casing to actually send to.
    private void addIfValid(Set<String> recipients, String email) {
        if (email == null || email.isBlank()) return;
        String normalized = email.trim();
        boolean alreadyPresent = recipients.stream().anyMatch(existing -> existing.equalsIgnoreCase(normalized));
        if (!alreadyPresent) {
            recipients.add(normalized);
        }
    }
}
