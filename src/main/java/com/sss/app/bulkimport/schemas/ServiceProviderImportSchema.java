package com.sss.app.bulkimport.schemas;

import com.sss.app.bulkimport.BulkImportSchema;
import com.sss.app.dto.library.serviceprovider.ServiceProviderCreateRequestDTO;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.service.library.serviceprovider.ServiceProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sss.app.bulkimport.RowUtils.blankToNull;

/**
 * Every field the Service Providers page's manual create/edit form exposes
 * (see ServiceProvidersPanel.tsx), so a bulk-imported provider has the same
 * field coverage as one created by hand.
 */
@Component
@RequiredArgsConstructor
public class ServiceProviderImportSchema implements BulkImportSchema {

    private static final Set<String> VALID_TYPES = Set.of("transport", "activity", "guide", "other");

    private final ServiceProviderService serviceProviderService;
    private final EscapePointRepository escapePointRepository;

    @Override
    public String entityType() {
        return "service-providers";
    }

    @Override
    public List<String> columns() {
        return List.of("name", "typeCode", "escapePointCode", "contactInfo", "countryCode", "status");
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("name", "typeCode");
    }

    @Override
    public List<String> validateRow(Map<String, String> row) {
        List<String> errors = new ArrayList<>();
        String typeCode = row.get("typeCode");
        if (typeCode != null && !typeCode.isBlank() && !VALID_TYPES.contains(typeCode.trim())) {
            errors.add("\"typeCode\" must be one of: transport, activity, guide, other");
        }
        String escapePointCode = row.get("escapePointCode");
        if (escapePointCode != null && !escapePointCode.isBlank() && escapePointRepository.findById(escapePointCode.trim()).isEmpty()) {
            errors.add("No escape point found with code \"" + escapePointCode + "\"");
        }
        return errors;
    }

    @Override
    public void commitRow(Map<String, String> row) {
        ServiceProviderCreateRequestDTO dto = new ServiceProviderCreateRequestDTO();
        dto.setName(row.get("name"));
        dto.setTypeCode(row.get("typeCode"));
        dto.setContactInfo(blankToNull(row.get("contactInfo")));
        dto.setCountryCode(blankToNull(row.get("countryCode")));
        dto.setStatus(blankToNull(row.get("status")));

        String escapePointCode = blankToNull(row.get("escapePointCode"));
        if (escapePointCode != null) {
            escapePointRepository.findById(escapePointCode).ifPresent(d -> dto.setEscapePointId(d.getUid()));
        }

        serviceProviderService.create(dto);
    }
}
