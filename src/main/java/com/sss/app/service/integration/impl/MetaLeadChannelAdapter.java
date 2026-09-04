package com.sss.app.service.integration.impl;

import com.sss.app.service.integration.LeadChannelAdapter;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.integration.meta.MetaFieldMappingResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Normalizes the MERGED envelope MetaWebhookOrchestrationService builds
 * (webhook ids: leadgen_id/page_id/form_id/ad_id/created_time, plus the
 * Graph API's fetched field_data[]) into the common NormalizedLeadPayload
 * contract. Meta's webhook alone never carries enough to normalize directly
 * — see RemoteLeadFetcher for why the Graph API fetch happens first.
 *
 * Expected rawPayload keys (set by the orchestration service, not Meta
 * itself): _orgId (Long), _pageId (String), leadgen_id, form_id, ad_id,
 * ad_name, adset_id, campaign_id, campaign_name, created_time, field_data
 * (List<Map<name, values>>).
 */
@Component
@RequiredArgsConstructor
public class MetaLeadChannelAdapter implements LeadChannelAdapter {

    private final MetaFieldMappingResolver fieldMappingResolver;

    @Override
    public String channelCode() {
        return "facebook";
    }

    @Override
    public NormalizedLeadPayload normalize(Map<String, Object> rawPayload) {
        Long orgId = (Long) rawPayload.get("_orgId");
        String formId = asString(rawPayload.get("form_id"));
        Map<String, String> fieldMapping = fieldMappingResolver.resolveMappingForForm(orgId, formId);

        NormalizedLeadPayload payload = new NormalizedLeadPayload();
        payload.setProvider(channelCode());
        payload.setSourceRefId(asString(rawPayload.get("leadgen_id")));
        payload.setFormId(formId);
        payload.setAdId(asString(rawPayload.get("ad_id")));
        payload.setCampaignId(asString(rawPayload.get("campaign_id")));
        payload.setCampaignName(asString(rawPayload.get("campaign_name")));
        payload.setPageId(asString(rawPayload.get("_pageId")));
        payload.setReceivedAt(parseCreatedTime(asString(rawPayload.get("created_time"))));
        payload.setRawPayload(rawPayload);

        StringBuilder unmapped = new StringBuilder();
        Object fieldDataObj = rawPayload.get("field_data");
        if (fieldDataObj instanceof List<?> fieldDataList) {
            for (Object entryObj : fieldDataList) {
                if (!(entryObj instanceof Map<?, ?> entry)) continue;
                String key = asString(entry.get("name"));
                if (key == null) continue;
                String value = firstValue(entry.get("values"));
                if (value == null) continue;

                String crmField = fieldMapping.get(key);
                if (crmField == null) {
                    appendUnmapped(unmapped, key, value);
                    continue;
                }
                applyToPayload(payload, crmField, value);
            }
        }
        if (unmapped.length() > 0) {
            payload.setNotes(unmapped.toString().strip());
        }

        return payload;
    }

    private void applyToPayload(NormalizedLeadPayload payload, String crmField, String value) {
        switch (crmField) {
            case "name" -> payload.setName(value);
            case "email" -> payload.setEmail(value);
            case "phone" -> payload.setPhone(value);
            case "destination_hint" -> payload.setDestinationHint(value);
            case "travel_date" -> payload.setTravelDate(parseDate(value));
            case "number_of_people" -> payload.setNumberOfPeople(parseInt(value));
            // "duration_days" (legacy admin-configured mapping code, kept
            // for existing org configurations) is a day count and needs the
            // "-1" conversion; "duration_nights" is already the value we
            // store, so it's taken as-is. The two are NOT interchangeable.
            case "duration_days" -> payload.setDurationNights(toNights(parseInt(value)));
            case "duration_nights" -> payload.setDurationNights(parseInt(value));
            case "notes" -> payload.setNotes(payload.getNotes() == null ? value : payload.getNotes() + "; " + value);
            case MetaFieldMappingResolver.CRM_FIELD_IGNORE -> { /* explicitly discarded by admin mapping */ }
            default -> { /* unknown crm_field value in a stored mapping row — ignore rather than fail the whole lead */ }
        }
    }

    private void appendUnmapped(StringBuilder builder, String key, String value) {
        if (builder.length() > 0) builder.append("\n");
        builder.append(key).append(": ").append(value);
    }

    private String firstValue(Object valuesObj) {
        if (valuesObj instanceof List<?> values && !values.isEmpty()) {
            return values.size() == 1 ? String.valueOf(values.get(0))
                    : values.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse(null);
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
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

    private Integer toNights(Integer days) {
        return days != null ? days - 1 : null;
    }

    private LocalDateTime parseCreatedTime(String value) {
        if (value == null) return null;
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
