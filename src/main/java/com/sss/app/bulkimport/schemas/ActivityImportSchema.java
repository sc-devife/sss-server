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
        return List.of("name", "destinationCode", "categoryCode", "durationMinutes", "basePrice", "description", "status");
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("name");
    }

    @Override
    public List<String> validateRow(Map<String, String> row) {
        List<String> errors = new ArrayList<>();
        String destinationCode = row.get("destinationCode");
        if (destinationCode != null && !destinationCode.isBlank() && findDestination(destinationCode).isEmpty()) {
            errors.add("No destination found with code \"" + destinationCode + "\"");
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

        String destinationCode = blankToNull(row.get("destinationCode"));
        if (destinationCode != null) {
            findDestination(destinationCode).ifPresent(d -> dto.setDestinationId(d.getUid()));
        }

        activityService.create(dto);
    }

    private Optional<EscapePoint> findDestination(String code) {
        return escapePointRepository.findAll().stream()
                .filter(e -> code.equals(e.getId()))
                .findFirst();
    }
}
