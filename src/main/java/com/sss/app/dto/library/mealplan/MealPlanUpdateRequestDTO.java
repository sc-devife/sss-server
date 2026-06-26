package com.sss.app.dto.library.mealplan;

import lombok.Data;

@Data
public class MealPlanUpdateRequestDTO {

    private String code;

    private String name;

    private String description;

    private Boolean isActive;
}
