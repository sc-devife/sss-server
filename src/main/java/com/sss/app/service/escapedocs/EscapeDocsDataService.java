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
 * Document needs (org bank details, a transport-only day grouping, the
 * Overview block's primary-traveller convenience alias) plus the five
 * section-visibility flags — same "wrap, don't re-query" pattern as
 * BillingDataService.
 */
@Service
@RequiredArgsConstructor
public class EscapeDocsDataService {

    private static final DateTimeFormatter DAY_DATE = DateTimeFormatter.ofPattern("d MMM yyyy");

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

    private String formatDate(Object value) {
        return value instanceof LocalDate date ? date.format(DAY_DATE) : null;
    }
}
