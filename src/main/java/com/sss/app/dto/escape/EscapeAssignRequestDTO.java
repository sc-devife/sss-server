package com.sss.app.dto.escape;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EscapeAssignRequestDTO {
    @NotNull(message = "userId is required")
    private Long userId;
    private String reason;
}
