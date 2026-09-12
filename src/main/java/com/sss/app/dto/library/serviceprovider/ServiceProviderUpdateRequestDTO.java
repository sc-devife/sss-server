package com.sss.app.dto.library.serviceprovider;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ServiceProviderUpdateRequestDTO {

    private String name;

    private String typeCode;

    private String contactInfo;

    private String contactName;

    private String contactNumber;

    @Email(message = "Invalid email")
    private String contactEmail;

    private String countryCode;

    private Integer quantity;

    private String otherTypeLabel;

    private String escapePointId;

    private String status;
}
