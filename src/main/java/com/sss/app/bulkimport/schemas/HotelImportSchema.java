package com.sss.app.bulkimport.schemas;

import com.sss.app.bulkimport.BulkImportSchema;
import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.location.LocationRepository;
import com.sss.app.service.library.hotel.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.sss.app.bulkimport.RowUtils.blankToNull;
import static com.sss.app.bulkimport.RowUtils.parseIntOrNull;

/**
 * Bulk import covers Hotel's core dictionary fields only (name, location,
 * destination, stars, address, contact, status) — meal plans, room types,
 * and images aren't spreadsheet-friendly relations and are left to the
 * regular create/edit screen, consistent with keeping this a v1-scoped
 * import rather than a full parity importer.
 */
@Component
@RequiredArgsConstructor
public class HotelImportSchema implements BulkImportSchema {

    private final HotelService hotelService;
    private final LocationRepository locationRepository;
    private final EscapePointRepository escapePointRepository;

    @Override
    public String entityType() {
        return "hotels";
    }

    @Override
    public List<String> columns() {
        return List.of("name", "locationDisplayName", "destinationCode", "stars", "address", "contactInfo", "status");
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("name", "locationDisplayName");
    }

    @Override
    public List<String> validateRow(Map<String, String> row) {
        List<String> errors = new ArrayList<>();
        String locationDisplayName = row.get("locationDisplayName");
        if (locationDisplayName != null && !locationDisplayName.isBlank()
                && locationRepository.findByDisplayNameIgnoreCase(locationDisplayName).isEmpty()) {
            errors.add("No location found named \"" + locationDisplayName + "\" — create it first on the Hotels screen");
        }
        String destinationCode = row.get("destinationCode");
        if (destinationCode != null && !destinationCode.isBlank() && findDestination(destinationCode).isEmpty()) {
            errors.add("No destination found with code \"" + destinationCode + "\"");
        }
        return errors;
    }

    @Override
    public void commitRow(Map<String, String> row) {
        HotelCreateRequestDTO dto = new HotelCreateRequestDTO();
        dto.setName(row.get("name"));
        dto.setStars(parseIntOrNull(row.get("stars")));
        dto.setAddress(blankToNull(row.get("address")));
        dto.setContactInfo(blankToNull(row.get("contactInfo")));
        dto.setStatus(blankToNull(row.get("status")));

        locationRepository.findByDisplayNameIgnoreCase(row.get("locationDisplayName"))
                .ifPresent(l -> dto.setLocationId(l.getUid()));

        String destinationCode = blankToNull(row.get("destinationCode"));
        if (destinationCode != null) {
            findDestination(destinationCode).ifPresent(d -> dto.setDestinationId(d.getUid()));
        }

        hotelService.create(dto);
    }

    private Optional<EscapePoint> findDestination(String code) {
        return escapePointRepository.findAll().stream()
                .filter(e -> code.equals(e.getId()))
                .findFirst();
    }
}
