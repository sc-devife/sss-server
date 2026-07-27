package com.sss.app.bulkimport.schemas;

import com.sss.app.bulkimport.BulkImportSchema;
import com.sss.app.dto.library.serviceprovider.ServiceProviderCreateRequestDTO;
import com.sss.app.service.library.serviceprovider.ServiceProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sss.app.bulkimport.RowUtils.blankToNull;

@Component
@RequiredArgsConstructor
public class ServiceProviderImportSchema implements BulkImportSchema {

    private static final Set<String> VALID_TYPES = Set.of("transport", "activity", "guide", "other");

    private final ServiceProviderService serviceProviderService;

    @Override
    public String entityType() {
        return "service-providers";
    }

    @Override
    public List<String> columns() {
        return List.of("name", "typeCode", "contactInfo", "countryCode", "status");
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
        serviceProviderService.create(dto);
    }
}
