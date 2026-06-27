package com.sss.app.dto.library.mealplan;

import lombok.Data;

import java.util.UUID;

@Data
public class MealPlanResponseDTO {

    private UUID uid;

    private String code;

    private String name;

    private String description;

    private Boolean isActive;
}
