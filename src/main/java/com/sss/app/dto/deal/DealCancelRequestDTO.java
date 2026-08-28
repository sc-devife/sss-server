package com.sss.app.dto.deal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DealCancelRequestDTO {
    @NotBlank(message = "A cancellation reason is required")
    private String reason;
}
