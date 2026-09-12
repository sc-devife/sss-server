package com.sss.app.service.escapedocs;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.helper.OrganizationsHelper;
import com.sss.app.service.BankAccountService;
import com.sss.app.service.quotationtemplate.QuotationDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Wraps QuotationDataService's data map with the extra fields the Escape
 * Document needs (org bank details, a transport-only day grouping, a flat
 * hotel-bookings list, a payment schedule summary, the Overview block's
 * primary-traveller convenience alias) plus the five section-visibility
 * flags — same "wrap, don't re-query" pattern as BillingDataService.
 */
@Service
@RequiredArgsConstructor
public class EscapeDocsDataService {

    private static final DateTimeFormatter DAY_DATE = DateTimeFormatter.ofPattern("d MMM yyyy");
    // "11/09/2026 (Friday)" — the Payment Schedule's own due-date format,
    // distinct from every other date in this document.
    private static final DateTimeFormatter DUE_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy (EEEE)");

    private final QuotationDataService quotationDataService;
    private final OrganizationsHelper organizationsHelper;
    private final BankAccountService bankAccountService;

    @SuppressWarnings("unchecked")
    public Map<String, Object> buildData(UUID escapeUid, DocSections sections) {
        Map<String, Object> data = quotationDataService.buildData(escapeUid);

        List<Map<String, Object>> travellers = (List<Map<String, Object>>) data.get("travellers");
        String primaryTravellerName = travellers.stream()
                .filter(t -> Boolean.TRUE.equals(t.get("isPrimary")))
                .map(t -> (String) t.get("name"))
                .findFirst()
                .orElse(null);
        data.put("primaryTravellerName", primaryTravellerName);

        // Mustache (mustache.java) has no "join a list"/"is this the last
        // item" construct — precomputed here rather than in the template.
        List<Map<String, Object>> escapePoints = (List<Map<String, Object>>) data.get("escapePoints");
        String escapePointNames = escapePoints == null || escapePoints.isEmpty()
                ? null
                : escapePoints.stream().map(ep -> (String) ep.get("name")).reduce((a, b) -> a + ", " + b).orElse(null);
        data.put("escapePointNames", escapePointNames);

        // Mustache can't format dates — pre-formatted here (mutating these
        // request-scoped maps in place is safe, they're freshly built per
        // call, same pattern BillingDataService uses for milestone amounts).
        data.put("startDateFormatted", formatDate(data.get("startDate")));
        data.put("endDateFormatted", formatDate(data.get("endDate")));

        List<Map<String, Object>> days = (List<Map<String, Object>>) data.get("days");
        if (days != null) {
            days.forEach(day -> day.put("dateFormatted", formatDate(day.get("date"))));
        }
        data.put("transportDays", buildTransportDays(days));

        // Always-shown sections (Greeting/Hotel Section/Payment Schedule) —
        // unlike the five checkbox-gated sections above, these are never
        // toggled off, same as the Overview block.
        List<Map<String, Object>> hotels = buildHotels(days);
        data.put("hotels", hotels);
        data.put("hasHotels", !hotels.isEmpty());
        data.put("paymentSchedule", buildPaymentSchedule(data));

        Organizations org = organizationsHelper.getMyOrganization();
        List<BankAccountDto> bankAccounts = bankAccountService.getAccountsForOrg(org.getUid());
        BankAccountDto bankAccount = bankAccounts.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .findFirst()
                .or(() -> bankAccounts.stream().findFirst())
                .orElse(null);
        if (bankAccount != null) {
            Map<String, Object> bank = new LinkedHashMap<>();
            bank.put("accountName", bankAccount.getAccountName());
            bank.put("accountNumber", bankAccount.getAccountNumber());
            bank.put("bankName", bankAccount.getBankName());
            bank.put("branchName", bankAccount.getBranchName());
            bank.put("ifsc", bankAccount.getIfsc());
            bank.put("swiftCode", bankAccount.getSwiftCode());
            data.put("bank", bank);
        }
        data.put("hasBankDetails", bankAccount != null);

        data.put("showTransports", sections.transports());
        data.put("showBankAccount", sections.bankAccount());
        data.put("showItinerary", sections.itinerary());
        data.put("showInclusionsExclusions", sections.inclusionsExclusions());
        data.put("showTermsAndConditions", sections.termsAndConditions());

        return data;
    }

    // A day-grouped, transport-only view of the itinerary for the dedicated
    // Transports section — deliberately not reusing QuotationDataService's
    // own transportRows, which already merges in activities/"other" items
    // and bakes in a table-rowspan layout that would need reworking to
    // exclude them. Days left with zero transport items are dropped
    // entirely rather than rendered empty.
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildTransportDays(List<Map<String, Object>> days) {
        if (days == null) {
            return List.of();
        }
        return days.stream()
                .map(day -> {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) day.get("items");
                    List<Map<String, Object>> transportItems = items.stream()
                            .filter(item -> item.containsKey("transport"))
                            .toList();
                    if (transportItems.isEmpty()) {
                        return null;
                    }
                    Map<String, Object> transportDay = new LinkedHashMap<>();
                    transportDay.put("dayNumber", day.get("dayNumber"));
                    transportDay.put("date", day.get("date"));
                    transportDay.put("dateFormatted", day.get("dateFormatted"));
                    transportDay.put("items", transportItems);
                    return transportDay;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // Flat list of hotel bookings across every day — Hotel/Check-in/
    // Check-out/Accommodation, the Hotel Section's own compact table
    // (distinct from the full day-wise Itinerary block).
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildHotels(List<Map<String, Object>> days) {
        if (days == null) {
            return List.of();
        }
        List<Map<String, Object>> hotels = new java.util.ArrayList<>();
        for (Map<String, Object> day : days) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) day.get("items");
            for (Map<String, Object> item : items) {
                Map<String, Object> hotel = (Map<String, Object>) item.get("hotel");
                if (hotel == null) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("hotelName", item.get("title"));
                // Rendered below the hotel name — stars (Integer count) for
                // Word, starIcons (one entry per star) for the HTML/PDF
                // template's {{#starIcons}}★{{/starIcons}} loop.
                row.put("stars", hotel.get("stars"));
                row.put("starIcons", hotel.get("starIcons"));
                row.put("checkIn", hotel.get("checkInFormatted"));
                row.put("checkOut", formatDate(hotel.get("checkOutDate")));
                row.put("accommodation", hotel.get("roomTypeName"));
                hotels.add(row);
            }
        }
        return hotels;
    }

    // Total (incl. tax)/per-person come straight from the existing pricing
    // block; amount received/due amount/due date are derived from the same
    // payment milestones QuotationDataService already assembles — same
    // computation BillingDataService uses for its own invoice summary, just
    // never throwing when there's no deal/quote yet (Docs must still render
    // something even before either exists).
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildPaymentSchedule(Map<String, Object> data) {
        Map<String, Object> pricing = (Map<String, Object>) data.get("pricing");
        Map<String, Object> payment = (Map<String, Object>) data.get("payment");
        List<Map<String, Object>> milestones = payment != null ? (List<Map<String, Object>>) payment.get("milestones") : null;
        if (milestones == null) {
            milestones = List.of();
        }

        java.math.BigDecimal total = asDecimal(pricing != null ? pricing.get("total") : null);
        java.math.BigDecimal amountReceived = milestones.stream()
                .map(m -> asDecimal(m.get("amountPaid")))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal dueAmount = total.subtract(amountReceived).max(java.math.BigDecimal.ZERO);
        LocalDate dueDate = milestones.stream()
                .filter(m -> !"paid".equals(m.get("status")))
                .map(m -> (LocalDate) m.get("dueDate"))
                .filter(java.util.Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);

        java.text.NumberFormat inrFormat = inrWholeFormat();
        Map<String, Object> schedule = new LinkedHashMap<>();
        schedule.put("totalFormatted", pricing != null ? pricing.get("totalFormatted") : null);
        schedule.put("perPaxFormatted", pricing != null ? pricing.get("perPaxFormatted") : null);
        schedule.put("amountReceivedFormatted", inrFormat.format(amountReceived));
        schedule.put("dueAmountFormatted", inrFormat.format(dueAmount));
        schedule.put("dueDateFormatted", dueDate != null ? dueDate.format(DUE_DATE) : null);
        schedule.put("hasDueDate", dueDate != null);
        return schedule;
    }

    private java.math.BigDecimal asDecimal(Object value) {
        return value instanceof java.math.BigDecimal d ? d : java.math.BigDecimal.ZERO;
    }

    private java.text.NumberFormat inrWholeFormat() {
        java.text.NumberFormat format = java.text.NumberFormat.getInstance(new java.util.Locale("en", "IN"));
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format;
    }

    private String formatDate(Object value) {
        return value instanceof LocalDate date ? date.format(DAY_DATE) : null;
    }
}
