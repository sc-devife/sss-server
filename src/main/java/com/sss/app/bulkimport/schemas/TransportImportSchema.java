package com.sss.app.bulkimport.schemas;

import com.sss.app.bulkimport.BulkImportSchema;
import com.sss.app.dto.library.transport.TransportCreateRequestDTO;
import com.sss.app.entity.library.serviceprovider.ServiceProvider;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.serviceprovider.ServiceProviderRepository;
import com.sss.app.service.library.transport.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.sss.app.bulkimport.RowUtils.blankToNull;
import static com.sss.app.bulkimport.RowUtils.parseDecimalOrNull;
import static com.sss.app.bulkimport.RowUtils.parseIntOrNull;

/**
 * Every field the Transport page's manual create/edit form exposes (see
 * TransportPanel.tsx), so a bulk-imported transport has the same field
 * coverage as one created by hand. No fields are left out here — unlike
 * Hotel/Activity/EscapePoint, Transport has no image upload or other
 * plain-text-unfriendly field.
 */
@Component
@RequiredArgsConstructor
public class TransportImportSchema implements BulkImportSchema {

    // Mirrors lib/transport-modes.ts's MODE_OPTIONS exactly — the previous
    // 5-value set (car, coach, flight, train, boat) silently rejected 15 of
    // the 20 modes the manual form actually supports, including common ones
    // like "bus" and "taxi" that were already in live use.
    private static final Set<String> VALID_MODES = Set.of(
            "flight", "train", "bus", "coach", "car", "taxi", "van", "boat", "ferry", "cruise",
            "helicopter", "motorcycle", "bicycle", "walking", "cable_car", "funicular",
            "camel", "horse", "atv", "other");

    private final TransportService transportService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final EscapePointRepository escapePointRepository;
    private final ImportCurrencySupport importCurrencySupport;

    @Override
    public String entityType() {
        return "transports";
    }

    @Override
    public List<String> columns() {
        return List.of("modeCode", "vehicleTypeCode", "capacity", "providerName", "basePrice", "priceCurrency",
                "pickupLocation", "dropLocation", "escapePointCode", "status");
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("modeCode");
    }

    @Override
    public List<String> validateRow(Map<String, String> row) {
        List<String> errors = new ArrayList<>();
        String modeCode = row.get("modeCode");
        if (modeCode != null && !modeCode.isBlank() && !VALID_MODES.contains(modeCode.trim())) {
            errors.add("\"modeCode\" must be one of: " + String.join(", ", VALID_MODES));
        }
        String providerName = row.get("providerName");
        if (providerName != null && !providerName.isBlank() && findProvider(providerName).isEmpty()) {
            errors.add("No service provider found named \"" + providerName + "\"");
        }
        String escapePointCode = row.get("escapePointCode");
        if (escapePointCode != null && !escapePointCode.isBlank() && escapePointRepository.findById(escapePointCode.trim()).isEmpty()) {
            errors.add("No escape point found with code \"" + escapePointCode + "\"");
        }
        importCurrencySupport.validate(row.get("priceCurrency")).ifPresent(errors::add);
        return errors;
    }

    @Override
    public void commitRow(Map<String, String> row) {
        TransportCreateRequestDTO dto = new TransportCreateRequestDTO();
        dto.setModeCode(row.get("modeCode"));
        dto.setVehicleTypeCode(blankToNull(row.get("vehicleTypeCode")));
        dto.setCapacity(parseIntOrNull(row.get("capacity")));
        dto.setBasePrice(parseDecimalOrNull(row.get("basePrice")));
        dto.setPriceCurrency(importCurrencySupport.normalize(row.get("priceCurrency")));
        dto.setStatus(blankToNull(row.get("status")));
        dto.setPickupLocation(blankToNull(row.get("pickupLocation")));
        dto.setDropLocation(blankToNull(row.get("dropLocation")));

        String providerName = blankToNull(row.get("providerName"));
        if (providerName != null) {
            findProvider(providerName).ifPresent(p -> dto.setProviderId(p.getUid()));
        }

        String escapePointCode = blankToNull(row.get("escapePointCode"));
        if (escapePointCode != null) {
            escapePointRepository.findById(escapePointCode).ifPresent(d -> dto.setEscapePointId(d.getUid()));
        }

        transportService.create(dto);
    }

    private Optional<ServiceProvider> findProvider(String name) {
        return serviceProviderRepository.findAll().stream()
                .filter(p -> name.equalsIgnoreCase(p.getName()))
                .findFirst();
    }
}
