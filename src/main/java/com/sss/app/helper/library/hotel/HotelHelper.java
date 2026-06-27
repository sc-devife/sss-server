package com.sss.app.helper.library.hotel;

import com.sss.app.entity.library.destination.Destination;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.entity.library.location.Location;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.roomtype.RoomType;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.repository.library.destination.DestinationRepository;
import com.sss.app.repository.library.location.LocationRepository;
import com.sss.app.repository.library.mealplan.MealPlanRepository;
import com.sss.app.repository.library.roomtype.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves relation IDs (locationId, destinationIds, mealPlanIds, roomTypeIds)
 * coming from request DTOs into managed entities, and validates they exist.
 * Keeps this lookup/validation logic out of the Service layer.
 */
@Component
@RequiredArgsConstructor
public class HotelHelper {

    private final LocationRepository locationRepository;
    private final DestinationRepository destinationRepository;
    private final MealPlanRepository mealPlanRepository;
    private final RoomTypeRepository roomTypeRepository;

    public Location resolveLocation(UUID locationId) {
        return locationRepository.findByUid(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));
    }

    public Set<Destination> resolveDestinations(Set<UUID> destinationIds) {
        if (destinationIds == null || destinationIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Destination> destinations = new HashSet<>(destinationRepository.findAllByUidIn(destinationIds));
        validateAllFound(destinationIds, destinations.size(), "Destination");
        return destinations;
    }

    public Set<MealPlan> resolveMealPlans(Set<UUID> mealPlanIds) {
        if (mealPlanIds == null || mealPlanIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<MealPlan> mealPlans = new HashSet<>(mealPlanRepository.findAllByUidIn(mealPlanIds));
        validateAllFound(mealPlanIds, mealPlans.size(), "MealPlan");
        return mealPlans;
    }

    public Set<RoomType> resolveRoomTypes(Set<UUID> roomTypeIds) {
        if (roomTypeIds == null || roomTypeIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<RoomType> roomTypes = new HashSet<>(roomTypeRepository.findAllByUidIn(roomTypeIds));
        validateAllFound(roomTypeIds, roomTypes.size(), "RoomType");
        return roomTypes;
    }

    /**
     * Applies all relations onto a Hotel entity in one go. Used by both create and update flows.
     * Pass null for any relation you don't want touched (relevant for partial updates).
     */
    public void applyRelations(Hotel hotel,
                                UUID locationId,
                                Set<UUID> destinationIds,
                                Set<UUID> mealPlanIds,
                                Set<UUID> roomTypeIds) {
        if (locationId != null) {
            hotel.setLocation(resolveLocation(locationId));
        }
        if (destinationIds != null) {
            hotel.setDestinations(resolveDestinations(destinationIds));
        }
        if (mealPlanIds != null) {
            hotel.setMealPlans(resolveMealPlans(mealPlanIds));
        }
        if (roomTypeIds != null) {
            hotel.setRoomTypes(resolveRoomTypes(roomTypeIds));
        }
    }

    private void validateAllFound(Set<UUID> requestedIds, int foundCount, String entityName) {
        if (foundCount != requestedIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more " + entityName + " IDs were not found: " + requestedIds);
        }
    }
}
