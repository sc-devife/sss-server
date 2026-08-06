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

    private String pickupLocation;

    private String dropLocation;

    // The EscapePoint's uid (String) — resolved to the entity in TransportHelper, matching Activity's escapePointId.
    private String escapePointId;

    private String status;
}
