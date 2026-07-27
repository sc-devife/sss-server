package com.sss.app.dto.library.serviceprovider;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ServiceProviderResponseDTO {

    private UUID uid;

    private String name;

    private String typeCode;

    private String contactInfo;

    private String countryCode;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
