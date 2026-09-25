package com.sss.app.bulkimport.schemas;

import com.sss.app.bulkimport.BulkImportSchema;
import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelRoomTypePricingRequestDTO;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.location.LocationRepository;
import com.sss.app.repository.library.mealplan.MealPlanRepository;
import com.sss.app.repository.library.roomtype.RoomTypeRepository;
import com.sss.app.repository.library.service.ServiceRepository;
import com.sss.app.service.library.hotel.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.sss.app.bulkimport.RowUtils.blankToNull;
import static com.sss.app.bulkimport.RowUtils.parseIntOrNull;
import static com.sss.app.bulkimport.RowUtils.parseLocalDateOrNull;
import static com.sss.app.bulkimport.RowUtils.parseLocalTimeOrNull;
import static com.sss.app.bulkimport.RowUtils.splitList;

/**
 * Every field HotelFormModal's manual create/edit form exposes, so a
 * bulk-imported hotel has the same field coverage as one created by hand.
 * Only "images" is left out — a file upload has no meaningful plain-text CSV
 * representation, and stays a manual-only, documented limitation.
 */
@Component
@RequiredArgsConstructor
public class HotelImportSchema implements BulkImportSchema {

    // Mirrors HotelFormModal.tsx's AMENITY_OPTIONS exactly.
    private static final Set<String> VALID_AMENITIES = Set.of(
            "wifi", "pool", "parking", "gym", "spa", "restaurant", "ac", "breakfast");

    private final HotelService hotelService;
    private final LocationRepository locationRepository;
    private final EscapePointRepository escapePointRepository;
    private final MealPlanRepository mealPlanRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ServiceRepository serviceRepository;
    private final ImportCurrencySupport importCurrencySupport;

    @Override
    public String entityType() {
        return "hotels";
    }

    @Override
    public List<String> columns() {
        return List.of(
                "name", "locationDisplayName", "escapePointCode", "stars", "address", "contactInfo",
                "mealPlanCodes", "roomTypeNames", "serviceNames",
                "checkInTime", "checkOutTime", "childAgeForExtraBed",
                "rateValidFrom", "rateValidTo", "amenities", "notes", "priceCurrency", "status");
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
        String escapePointCode = row.get("escapePointCode");
        if (escapePointCode != null && !escapePointCode.isBlank() && escapePointRepository.findById(escapePointCode.trim()).isEmpty()) {
            errors.add("No escape point found with code \"" + escapePointCode + "\"");
        }

        for (String code : splitList(row.get("mealPlanCodes"))) {
            if (mealPlanRepository.findByCodeIgnoreCase(code).isEmpty()) {
                errors.add("No meal plan found with code \"" + code + "\"");
            }
        }
        for (String name : splitList(row.get("roomTypeNames"))) {
            if (roomTypeRepository.findByNameIgnoreCase(name).isEmpty()) {
                errors.add("No room type found named \"" + name + "\"");
            }
        }
        for (String name : splitList(row.get("serviceNames"))) {
            if (serviceRepository.findByNameIgnoreCaseAndHotelIsNull(name).isEmpty()) {
                errors.add("No service found named \"" + name + "\"");
            }
        }
        for (String amenity : splitList(row.get("amenities"))) {
            if (!VALID_AMENITIES.contains(amenity)) {
                errors.add("\"" + amenity + "\" is not a valid amenity — must be one of: " + String.join(", ", VALID_AMENITIES));
            }
        }

        String checkInRaw = row.get("checkInTime");
        if (checkInRaw != null && !checkInRaw.isBlank() && parseLocalTimeOrNull(checkInRaw) == null) {
            errors.add("\"checkInTime\" must be in HH:mm format (e.g. 14:00)");
        }
        String checkOutRaw = row.get("checkOutTime");
        if (checkOutRaw != null && !checkOutRaw.isBlank() && parseLocalTimeOrNull(checkOutRaw) == null) {
            errors.add("\"checkOutTime\" must be in HH:mm format (e.g. 11:00)");
        }
        String fromRaw = row.get("rateValidFrom");
        if (fromRaw != null && !fromRaw.isBlank() && parseLocalDateOrNull(fromRaw) == null) {
            errors.add("\"rateValidFrom\" must be in YYYY-MM-DD format");
        }
        String toRaw = row.get("rateValidTo");
        if (toRaw != null && !toRaw.isBlank() && parseLocalDateOrNull(toRaw) == null) {
            errors.add("\"rateValidTo\" must be in YYYY-MM-DD format");
        }
        importCurrencySupport.validate(row.get("priceCurrency")).ifPresent(errors::add);
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
        dto.setCheckInTime(parseLocalTimeOrNull(row.get("checkInTime")));
        dto.setCheckOutTime(parseLocalTimeOrNull(row.get("checkOutTime")));
        dto.setChildAgeForExtraBed(blankToNull(row.get("childAgeForExtraBed")));
        dto.setRateValidFrom(parseLocalDateOrNull(row.get("rateValidFrom")));
        dto.setRateValidTo(parseLocalDateOrNull(row.get("rateValidTo")));
        dto.setNotes(blankToNull(row.get("notes")));
        dto.setPriceCurrency(importCurrencySupport.normalize(row.get("priceCurrency")));

        List<String> amenities = splitList(row.get("amenities"));
        if (!amenities.isEmpty()) {
            dto.setAmenities(amenities);
        }

        locationRepository.findByDisplayNameIgnoreCase(row.get("locationDisplayName"))
                .ifPresent(l -> dto.setLocationId(l.getUid()));

        String escapePointCode = blankToNull(row.get("escapePointCode"));
        if (escapePointCode != null) {
            escapePointRepository.findById(escapePointCode).ifPresent(d -> dto.setEscapePointId(d.getUid()));
        }

        Set<UUID> mealPlanIds = new HashSet<>();
        for (String code : splitList(row.get("mealPlanCodes"))) {
            mealPlanRepository.findByCodeIgnoreCase(code).ifPresent(m -> mealPlanIds.add(m.getUid()));
        }
        if (!mealPlanIds.isEmpty()) dto.setMealPlanIds(mealPlanIds);

        // No per-room-type price column in the CSV — bulk-imported room
        // types start priceless, same as any hotel-level field the sheet
        // doesn't cover; price is filled in later via the Edit Hotel popup.
        List<HotelRoomTypePricingRequestDTO> roomTypePricing = new ArrayList<>();
        for (String name : splitList(row.get("roomTypeNames"))) {
            roomTypeRepository.findByNameIgnoreCase(name)
                    .ifPresent(r -> roomTypePricing.add(pricingRow(r.getUid(), null)));
        }
        if (!roomTypePricing.isEmpty()) dto.setRoomTypePricing(roomTypePricing);

        Set<UUID> serviceIds = new HashSet<>();
        for (String name : splitList(row.get("serviceNames"))) {
            serviceRepository.findByNameIgnoreCaseAndHotelIsNull(name).ifPresent(s -> serviceIds.add(s.getUid()));
        }
        if (!serviceIds.isEmpty()) dto.setServiceIds(serviceIds);

        hotelService.create(dto);
    }

    private HotelRoomTypePricingRequestDTO pricingRow(UUID roomTypeId, java.math.BigDecimal price) {
        HotelRoomTypePricingRequestDTO row = new HotelRoomTypePricingRequestDTO();
        row.setRoomTypeId(roomTypeId);
        row.setPrice(price);
        return row;
    }
}
