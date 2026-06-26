package com.sss.app.dto.library.mealplan;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MealPlanCreateRequestDTO {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
