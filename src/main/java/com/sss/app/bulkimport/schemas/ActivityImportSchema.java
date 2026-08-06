package com.sss.app.bulkimport.schemas;

import com.sss.app.bulkimport.BulkImportSchema;
import com.sss.app.dto.library.activity.ActivityCreateRequestDTO;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.service.library.activity.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.sss.app.bulkimport.RowUtils.blankToNull;
import static com.sss.app.bulkimport.RowUtils.parseDecimalOrNull;
import static com.sss.app.bulkimport.RowUtils.parseIntOrNull;

@Component
@RequiredArgsConstructor
public class ActivityImportSchema implements BulkImportSchema {

    private final ActivityService activityService;
    private final EscapePointRepository escapePointRepository;

    @Override
    public String entityType() {
        return "activities";
    }

    @Override
    public List<String> columns() {
        return List.of("name", "escapePointCode", "categoryCode", "durationMinutes", "basePrice", "description", "status");
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("name");
    }

    @Override
    public List<String> validateRow(Map<String, String> row) {
        List<String> errors = new ArrayList<>();
        String escapePointCode = row.get("escapePointCode");
        if (escapePointCode != null && !escapePointCode.isBlank() && findEscapePoint(escapePointCode).isEmpty()) {
            errors.add("No escape point found with code \"" + escapePointCode + "\"");
        }
        return errors;
    }

    @Override
    public void commitRow(Map<String, String> row) {
        ActivityCreateRequestDTO dto = new ActivityCreateRequestDTO();
        dto.setName(row.get("name"));
        dto.setCategoryCode(blankToNull(row.get("categoryCode")));
        dto.setDurationMinutes(parseIntOrNull(row.get("durationMinutes")));
        dto.setBasePrice(parseDecimalOrNull(row.get("basePrice")));
        dto.setDescription(blankToNull(row.get("description")));
        dto.setStatus(blankToNull(row.get("status")));

        String escapePointCode = blankToNull(row.get("escapePointCode"));
        if (escapePointCode != null) {
            findEscapePoint(escapePointCode).ifPresent(d -> dto.setEscapePointId(d.getUid()));
        }

        activityService.create(dto);
    }

    private Optional<EscapePoint> findEscapePoint(String code) {
        return escapePointRepository.findAll().stream()
                .filter(e -> code.equals(e.getId()))
                .findFirst();
    }
}
