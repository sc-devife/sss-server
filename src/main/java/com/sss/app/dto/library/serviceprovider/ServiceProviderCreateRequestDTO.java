package com.sss.app.dto.library.serviceprovider;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServiceProviderCreateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Type is required")
    private String typeCode;

    private String contactInfo;

    private String contactName;

    private String contactNumber;

    @Email(message = "Invalid email")
    private String contactEmail;

    private String countryCode;

    private Integer quantity;

    private String otherTypeLabel;

    // The EscapePoint's uid (String) — resolved to the entity in the service
    // layer, matching Transport/Activity's escapePointId.
    private String escapePointId;

    private String status;
}
