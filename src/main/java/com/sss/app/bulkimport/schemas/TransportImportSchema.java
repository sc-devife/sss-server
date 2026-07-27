package com.sss.app.bulkimport.schemas;

import com.sss.app.bulkimport.BulkImportSchema;
import com.sss.app.dto.library.transport.TransportCreateRequestDTO;
import com.sss.app.entity.library.serviceprovider.ServiceProvider;
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

@Component
@RequiredArgsConstructor
public class TransportImportSchema implements BulkImportSchema {

    private static final Set<String> VALID_MODES = Set.of("car", "coach", "flight", "train", "boat");

    private final TransportService transportService;
    private final ServiceProviderRepository serviceProviderRepository;

    @Override
    public String entityType() {
        return "transports";
    }

    @Override
    public List<String> columns() {
        return List.of("modeCode", "vehicleTypeCode", "capacity", "providerName", "basePrice", "status");
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
            errors.add("\"modeCode\" must be one of: car, coach, flight, train, boat");
        }
        String providerName = row.get("providerName");
        if (providerName != null && !providerName.isBlank() && findProvider(providerName).isEmpty()) {
            errors.add("No service provider found named \"" + providerName + "\"");
        }
        return errors;
    }

    @Override
    public void commitRow(Map<String, String> row) {
        TransportCreateRequestDTO dto = new TransportCreateRequestDTO();
        dto.setModeCode(row.get("modeCode"));
        dto.setVehicleTypeCode(blankToNull(row.get("vehicleTypeCode")));
        dto.setCapacity(parseIntOrNull(row.get("capacity")));
        dto.setBasePrice(parseDecimalOrNull(row.get("basePrice")));
        dto.setStatus(blankToNull(row.get("status")));

        String providerName = blankToNull(row.get("providerName"));
        if (providerName != null) {
            findProvider(providerName).ifPresent(p -> dto.setProviderId(p.getUid()));
        }

        transportService.create(dto);
    }

    private Optional<ServiceProvider> findProvider(String name) {
        return serviceProviderRepository.findAll().stream()
                .filter(p -> name.equalsIgnoreCase(p.getName()))
                .findFirst();
    }
}
