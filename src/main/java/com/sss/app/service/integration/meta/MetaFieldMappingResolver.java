package com.sss.app.service.integration.meta;

import com.sss.app.entity.integration.meta.MetaFieldMapping;
import com.sss.app.repository.integration.meta.MetaFieldMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves Meta field_data question keys to CRM lead fields for a given
 * (org, form). Priority: form-specific DB row > org-wide default DB row
 * (form_id IS NULL) > this hardcoded fallback — so the pipeline works
 * correctly even before any admin configures a mapping.
 */
@Component
@RequiredArgsConstructor
public class MetaFieldMappingResolver {

    public static final String CRM_FIELD_IGNORE = "ignore";

    private static final Map<String, String> DEFAULT_MAPPING = Map.of(
            "full_name", "name",
            "first_name", "name",
            "email", "email",
            "phone_number", "phone",
            "city", "destination_hint"
    );

    private final MetaFieldMappingRepository mappingRepository;

    public Map<String, String> resolveMappingForForm(Long orgId, String formId) {
        Map<String, String> resolved = new HashMap<>(DEFAULT_MAPPING);

        for (MetaFieldMapping mapping : mappingRepository.findAllByOrgIdAndFormIdIsNull(orgId)) {
            resolved.put(mapping.getMetaFieldKey(), mapping.getCrmField());
        }

        if (formId != null) {
            for (MetaFieldMapping mapping : mappingRepository.findAllByOrgIdAndFormId(orgId, formId)) {
                resolved.put(mapping.getMetaFieldKey(), mapping.getCrmField());
            }
        }

        return resolved;
    }
}
