package com.sss.app.dto.library.transport;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransportUpdateRequestDTO {

    private String modeCode;

    private String vehicleTypeCode;

    private Integer capacity;

    private UUID providerId;

    private BigDecimal basePrice;

    private String pickupLocation;

    private String dropLocation;

    private String destinationId;

    private String status;
}
