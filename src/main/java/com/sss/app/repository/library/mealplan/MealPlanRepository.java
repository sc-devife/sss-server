package com.sss.app.repository.library.mealplan;

import com.sss.app.entity.library.mealplan.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    Optional<MealPlan> findByUid(UUID uid);

    List<MealPlan> findAllByUidIn(Set<UUID> uids);

    Optional<MealPlan> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
