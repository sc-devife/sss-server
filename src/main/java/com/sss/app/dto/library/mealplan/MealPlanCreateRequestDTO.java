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

    // Set only when created for one specific hotel (a custom, hotel-only meal plan that
    // never appears in the library). Omitted for a library meal plan.
    private java.util.UUID hotelId;
}
