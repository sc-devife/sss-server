package com.sss.app.dto.library.transport;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransportCreateRequestDTO {

    @NotBlank(message = "Mode is required")
    private String modeCode;

    private String vehicleTypeCode;

    private Integer capacity;

    private UUID providerId;

    private BigDecimal basePrice;

    private String status;
}
