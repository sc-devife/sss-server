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

    @Override
    public List<String> columns() {
        return List.of("code", "name", "countryCode", "regionCode", "cityCode", "description", "status");
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
        dto.setCountryCode(blankToNull(row.get("countryCode")));
        dto.setRegionCode(blankToNull(row.get("regionCode")));
        dto.setCityCode(blankToNull(row.get("cityCode")));
        dto.setDescription(blankToNull(row.get("description")));
        dto.setStatus(blankToNull(row.get("status")));
        escapePointsService.createEscapePoint(dto);
    }
}
