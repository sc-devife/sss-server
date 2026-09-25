package com.sss.app.repository.library.mealplan;

import com.sss.app.entity.library.mealplan.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    Optional<MealPlan> findByUid(UUID uid);

    List<MealPlan> findAllByUidIn(Set<UUID> uids);

    // Global library plans only — bulk import and the main Meal Plans module.
    Optional<MealPlan> findByCodeIgnoreCaseAndHotelIsNull(String code);

    boolean existsByCodeIgnoreCaseAndHotelIsNull(String code);

    List<MealPlan> findAllByHotelIsNull();

    @Query("SELECT m FROM MealPlan m LEFT JOIN m.hotel h WHERE m.hotel IS NULL OR h.uid = :hotelUid")
    List<MealPlan> findAllVisibleToHotel(@Param("hotelUid") UUID hotelUid);

    @Query("SELECT COUNT(m) > 0 FROM MealPlan m LEFT JOIN m.hotel h WHERE LOWER(m.code) = LOWER(:code) AND (m.hotel IS NULL OR h.uid = :hotelUid)")
    boolean existsByCodeIgnoreCaseVisibleToHotel(@Param("code") String code, @Param("hotelUid") UUID hotelUid);
}
