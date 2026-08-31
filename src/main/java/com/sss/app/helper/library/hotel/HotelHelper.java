package com.sss.app.helper.library.hotel;

import com.sss.app.entity.library.activity.Activity;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.entity.library.location.Location;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.roomtype.RoomType;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.repository.library.activity.ActivityRepository;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.location.LocationRepository;
import com.sss.app.repository.library.mealplan.MealPlanRepository;
import com.sss.app.repository.library.roomtype.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
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
    private final ActivityRepository activityRepository;

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

    public Set<RoomType> resolveRoomTypes(Set<UUID> roomTypeIds) {
        if (roomTypeIds == null || roomTypeIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<RoomType> roomTypes = new HashSet<>(roomTypeRepository.findAllByUidIn(roomTypeIds));
        validateAllFound(roomTypeIds, roomTypes.size(), "RoomType");
        return roomTypes;
    }

    public Set<Activity> resolveActivities(Set<UUID> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Activity> activities = new HashSet<>(activityRepository.findAllByUidIn(new java.util.ArrayList<>(activityIds)));
        validateAllFound(activityIds, activities.size(), "Activity");
        return activities;
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
                                Set<UUID> roomTypeIds,
                                Set<UUID> activityIds) {
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
        if (roomTypeIds != null) {
            hotel.setRoomTypes(resolveRoomTypes(roomTypeIds));
        }
        if (activityIds != null) {
            hotel.setActivities(resolveActivities(activityIds));
        }
    }

    private void validateAllFound(Set<?> requestedIds, int foundCount, String entityName) {
        if (foundCount != requestedIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more " + entityName + " IDs were not found: " + requestedIds);
        }
    }
}
