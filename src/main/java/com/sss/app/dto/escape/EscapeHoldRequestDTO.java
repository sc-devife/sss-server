package com.sss.app.dto.escape;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EscapeHoldRequestDTO {
    @NotNull(message = "Hold date is required")
    private LocalDate holdDate;
}
