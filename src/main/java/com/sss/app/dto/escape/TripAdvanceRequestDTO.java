package com.sss.app.dto.escape;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TripAdvanceRequestDTO {
    @NotBlank(message = "targetStatus is required")
    private String targetStatus;
}
