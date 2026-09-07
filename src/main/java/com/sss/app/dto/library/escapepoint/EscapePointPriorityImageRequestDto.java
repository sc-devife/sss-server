package com.sss.app.dto.library.escapepoint;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EscapePointPriorityImageRequestDto {
    @NotBlank(message = "imageUrl is required")
    private String imageUrl;
}
