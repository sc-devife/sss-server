package com.sss.app.dto.library.serviceprovider;

import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ServiceProviderResponseDTO {

    private UUID uid;

    private String name;

    private String typeCode;

    private String contactInfo;

    private String contactName;

    private String contactNumber;

    private String contactEmail;

    private String countryCode;

    private Integer quantity;

    private String otherTypeLabel;

    private EscapePointResponseDto escapePoint;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
