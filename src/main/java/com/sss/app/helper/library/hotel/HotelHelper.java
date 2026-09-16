package com.sss.app.helper.library.hotel;

import com.sss.app.dto.library.hotel.HotelRoomTypePricingRequestDTO;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.entity.library.hotel.HotelRoomType;
import com.sss.app.entity.library.location.Location;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.roomtype.RoomType;
import com.sss.app.entity.library.service.Service;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.location.LocationRepository;
import com.sss.app.repository.library.mealplan.MealPlanRepository;
import com.sss.app.repository.library.roomtype.RoomTypeRepository;
import com.sss.app.repository.library.service.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves relation IDs (locationId, escapePointId, mealPlanIds, roomTypeIds)
 * coming from request DTOs into managed entities, and validates they exist.
 * Keeps this lookup/validation logic out of the Service layer.
 */
@Component
@RequiredArgsConstructor
public class HotelHelper {

    private final LocationRepository locationRepository;
    private final EscapePointRepository escapePointRepository;
    private final MealPlanRepository mealPlanRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ServiceRepository serviceRepository;

    public Location resolveLocation(UUID locationId) {
        return locationRepository.findByUid(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));
    }

    public EscapePoint resolveEscapePoint(String escapePointUid) {
        return escapePointRepository.findByUid(escapePointUid)
                .orElseThrow(() -> new ResourceNotFoundException("EscapePoint", escapePointUid));
    }

    public Set<EscapePoint> resolveEscapePoints(Set<String> escapePointIds) {
        if (escapePointIds == null || escapePointIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<EscapePoint> escapePoints = new HashSet<>(escapePointRepository.findAllByUidIn(escapePointIds));
        validateAllFound(escapePointIds, escapePoints.size(), "EscapePoint");
        return escapePoints;
    }

    public Set<MealPlan> resolveMealPlans(Set<UUID> mealPlanIds) {
        if (mealPlanIds == null || mealPlanIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<MealPlan> mealPlans = new HashSet<>(mealPlanRepository.findAllByUidIn(mealPlanIds));
        validateAllFound(mealPlanIds, mealPlans.size(), "MealPlan");
        return mealPlans;
    }

    // Reconciles the hotel's room-type pricing list against what was
    // submitted: updates the price on pairs that are kept, removes pairs
    // that were dropped, adds pairs that are new. Deliberately NOT a
    // clear()-then-re-add of the whole collection — for a
    // cascade="all-delete-orphan" collection with a (hotel_id, room_type_id)
    // unique constraint, clearing and immediately re-inserting the same
    // pair within one flush can violate that constraint (Hibernate doesn't
    // guarantee the DELETE for the cleared row lands before the INSERT for
    // its replacement), so unchanged/kept pairs must be mutated in place
    // instead of being deleted and recreated.
    public void applyRoomTypePricing(Hotel hotel, List<HotelRoomTypePricingRequestDTO> pricing) {
        Set<UUID> requestedIds = new HashSet<>();
        for (HotelRoomTypePricingRequestDTO row : pricing) {
            if (!requestedIds.add(row.getRoomTypeId())) {
                throw new BadRequestException("The same room type was added more than once");
            }
        }

        Set<RoomType> roomTypes = requestedIds.isEmpty()
                ? new HashSet<>()
                : new HashSet<>(roomTypeRepository.findAllByUidIn(requestedIds));
        validateAllFound(requestedIds, roomTypes.size(), "RoomType");
        java.util.Map<UUID, RoomType> roomTypeByUid = new java.util.HashMap<>();
        roomTypes.forEach(rt -> roomTypeByUid.put(rt.getUid(), rt));

        Set<HotelRoomType> current = hotel.getRoomTypes();
        java.util.Map<UUID, HotelRoomType> currentByRoomTypeUid = new java.util.HashMap<>();
        current.forEach(hrt -> currentByRoomTypeUid.put(hrt.getRoomType().getUid(), hrt));

        current.removeIf(hrt -> !requestedIds.contains(hrt.getRoomType().getUid()));

        for (HotelRoomTypePricingRequestDTO row : pricing) {
            HotelRoomType existing = currentByRoomTypeUid.get(row.getRoomTypeId());
            if (existing != null) {
                existing.setPrice(row.getPrice());
            } else {
                current.add(HotelRoomType.builder()
                        .hotel(hotel)
                        .roomType(roomTypeByUid.get(row.getRoomTypeId()))
                        .price(row.getPrice())
                        .build());
            }
        }
    }

    public Set<Service> resolveServices(Set<UUID> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Service> services = new HashSet<>(serviceRepository.findAllByUidIn(serviceIds));
        validateAllFound(serviceIds, services.size(), "Service");
        return services;
    }

    /**
     * Applies all relations onto a Hotel entity in one go. Used by both create and update flows.
     * Pass null for any relation you don't want touched (relevant for partial updates).
     */
    public void applyRelations(Hotel hotel,
                                UUID locationId,
                                String escapePointId,
                                Set<String> escapePointIds,
                                Set<UUID> mealPlanIds,
                                List<HotelRoomTypePricingRequestDTO> roomTypePricing,
                                Set<UUID> serviceIds) {
        if (locationId != null) {
            hotel.setLocation(resolveLocation(locationId));
        }
        if (escapePointId != null) {
            hotel.setEscapePoint(resolveEscapePoint(escapePointId));
        }
        if (escapePointIds != null) {
            hotel.setEscapePoints(resolveEscapePoints(escapePointIds));
        }
        if (mealPlanIds != null) {
            hotel.setMealPlans(resolveMealPlans(mealPlanIds));
        }
        if (roomTypePricing != null) {
            applyRoomTypePricing(hotel, roomTypePricing);
        }
        if (serviceIds != null) {
            hotel.setServices(resolveServices(serviceIds));
        }
    }

    private void validateAllFound(Set<?> requestedIds, int foundCount, String entityName) {
        if (foundCount != requestedIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more " + entityName + " IDs were not found: " + requestedIds);
        }
    }
}
