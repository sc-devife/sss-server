package com.sss.app.dto.library.transport;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransportUpdateRequestDTO {

    private String modeCode;

    private String vehicleTypeCode;

    private String vehicleNumber;

    private Integer capacity;

    private String ownerType;

    private UUID providerId;

    private BigDecimal basePrice;

    private String pickupLocation;

    private String dropLocation;

    private String contactName;

    private String contactNumber;

    @Email(message = "Invalid email")
    private String contactEmail;

    private String escapePointId;

    private String status;
}
