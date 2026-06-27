package com.sss.app.mapper.library.mealplan;

import com.sss.app.dto.library.mealplan.MealPlanCreateRequestDTO;
import com.sss.app.dto.library.mealplan.MealPlanResponseDTO;
import com.sss.app.dto.library.mealplan.MealPlanUpdateRequestDTO;
import com.sss.app.entity.library.mealplan.MealPlan;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MealPlanMapper {

    MealPlan toEntityCreate(MealPlanCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(MealPlanUpdateRequestDTO dto, @MappingTarget MealPlan entity);

    MealPlanResponseDTO toResponse(MealPlan entity);
}
