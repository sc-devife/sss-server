package com.sss.app.bulkimport.schemas;

import com.sss.app.bulkimport.BulkImportSchema;
import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointLocationsUpdateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.helper.library.escapepoint.EscapePointsHelper;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.location.LocationRepository;
import com.sss.app.service.library.escapepoint.EscapePointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sss.app.bulkimport.RowUtils.blankToNull;

/**
 * Every field EscapePointFormModal's manual create/edit form exposes, so a
 * bulk-imported escape point has the same field coverage as one created by
 * hand. "images" is left out — a file upload has no meaningful plain-text
 * CSV representation, and stays a manual-only, documented limitation.
 * "nick_name", "tags" and "remarks" exist on the entity/DTO but aren't
 * exposed in the manual form either, so there's no manual/bulk gap to close
 * for them — left out of the template too.
 */
@Component
@RequiredArgsConstructor
public class EscapePointImportSchema implements BulkImportSchema {

    private final EscapePointsService escapePointsService;
    private final EscapePointRepository escapePointRepository;
    private final LocationRepository locationRepository;
    private final EscapePointsHelper escapePointsHelper;

    @Override
    public String entityType() {
        return "escape-points";
    }

    @Override
    public List<String> columns() {
        return List.of("code", "name", "locationDisplayName", "description",
                "nearest_airport", "currency", "time_zone", "status");
    }

    @Override
    public List<String> requiredColumns() {
        // locationDisplayName mirrors the manual form's own required Location
        // field (EscapePointFormModal.tsx) — a location is not optional there.
        return List.of("code", "name", "locationDisplayName");
    }

    @Override
    public List<String> validateRow(Map<String, String> row) {
        List<String> errors = new ArrayList<>();
        String code = row.get("code");
        if (code != null && !code.isBlank() && escapePointRepository.existsById(code)) {
            errors.add("Code \"" + code + "\" already exists");
        }
        String locationDisplayName = row.get("locationDisplayName");
        if (locationDisplayName != null && !locationDisplayName.isBlank()
                && locationRepository.findByDisplayNameIgnoreCase(locationDisplayName).isEmpty()) {
            errors.add("No location found named \"" + locationDisplayName + "\" — create it first on the Hotels screen");
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
        dto.setNearest_airport(blankToNull(row.get("nearest_airport")));
        dto.setCurrency(blankToNull(row.get("currency")));
        dto.setTime_zone(blankToNull(row.get("time_zone")));

        EscapePointResponseDto created = escapePointsService.createEscapePoint(dto);

        String locationDisplayName = blankToNull(row.get("locationDisplayName"));
        if (locationDisplayName != null) {
            locationRepository.findByDisplayNameIgnoreCase(locationDisplayName).ifPresent(location -> {
                String locationUid = location.getUid().toString();
                EscapePointLocationsUpdateRequestDto locationsDto = new EscapePointLocationsUpdateRequestDto();
                locationsDto.setLocationUids(List.of(locationUid));
                locationsDto.setPrimaryLocationUid(locationUid);
                escapePointsHelper.reassignLocations(created.getUid(), locationsDto);
            });
        }
    }
}
