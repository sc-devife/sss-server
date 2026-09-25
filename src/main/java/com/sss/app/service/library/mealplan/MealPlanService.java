package com.sss.app.service.library.mealplan;

import com.sss.app.dto.library.mealplan.MealPlanCreateRequestDTO;
import com.sss.app.dto.library.mealplan.MealPlanResponseDTO;
import com.sss.app.dto.library.mealplan.MealPlanUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface MealPlanService {

    MealPlanResponseDTO create(MealPlanCreateRequestDTO dto);

    MealPlanResponseDTO getById(UUID id);

    // hotelId null -> library (global) plans; set -> library plans plus that hotel's own custom plans.
    List<MealPlanResponseDTO> getAll(UUID hotelId);

    MealPlanResponseDTO update(UUID id, MealPlanUpdateRequestDTO dto);

    void delete(UUID id);
}
