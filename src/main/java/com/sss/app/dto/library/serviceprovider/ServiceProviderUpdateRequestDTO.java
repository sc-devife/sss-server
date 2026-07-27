package com.sss.app.dto.library.serviceprovider;

import lombok.Data;

@Data
public class ServiceProviderUpdateRequestDTO {

    private String name;

    private String typeCode;

    private String contactInfo;

    private String countryCode;

    private String status;
}
