package com.sss.app.dto.library.hotel;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HotelPriorityImageRequestDTO {
    @NotBlank(message = "imageUrl is required")
    private String imageUrl;
}
