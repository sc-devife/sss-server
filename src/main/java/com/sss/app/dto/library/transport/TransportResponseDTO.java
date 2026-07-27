package com.sss.app.dto.library.transport;

import com.sss.app.dto.library.serviceprovider.ServiceProviderResponseDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransportResponseDTO {

    private UUID uid;

    private String modeCode;

    private String vehicleTypeCode;

    private Integer capacity;

    private ServiceProviderResponseDTO provider;

    private BigDecimal basePrice;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
