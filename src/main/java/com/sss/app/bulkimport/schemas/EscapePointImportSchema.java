package com.sss.app.bulkimport.schemas;

import com.sss.app.bulkimport.BulkImportSchema;
import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.service.library.escapepoint.EscapePointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sss.app.bulkimport.RowUtils.blankToNull;

@Component
@RequiredArgsConstructor
public class EscapePointImportSchema implements BulkImportSchema {

    private final EscapePointsService escapePointsService;
    private final EscapePointRepository escapePointRepository;

    @Override
    public String entityType() {
        return "escape-points";
    }

    // countryCode/regionCode/cityCode dropped along with the EscapePoint
    // fields they populated — location coverage is now set via the
    // EscapePoint <-> Location relation (Locations panel), not bulk import.
    @Override
    public List<String> columns() {
        return List.of("code", "name", "description", "status");
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("code", "name");
    }

    @Override
    public List<String> validateRow(Map<String, String> row) {
        List<String> errors = new ArrayList<>();
        String code = row.get("code");
        if (code != null && !code.isBlank() && escapePointRepository.existsById(code)) {
            errors.add("Code \"" + code + "\" already exists");
        }
        return errors;
    }

    @Override
    public void commitRow(Map<String, String> row) {
        EscapePointCreateRequestDto dto = new EscapePointCreateRequestDto();
        dto.setId(row.get("code"));
        dto.setName(row.get("name"));
        dto.setDescription(blankToNull(row.get("description")));
        dto.setStatus(blankToNull(row.get("status")));
        escapePointsService.createEscapePoint(dto);
    }
}
