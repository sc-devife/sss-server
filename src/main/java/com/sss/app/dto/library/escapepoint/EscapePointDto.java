package com.sss.app.dto.library.escapepoint;

import lombok.Data;

import java.util.List;

@Data
public class EscapePointDto {
    private String id;
    private String name;
    private String nick_name;
    private String city;
    private String province;
    private String country;
    private String region;
    private String nearest_airport;
    private String currency;
    private String time_zone;
    private String tags;
    private String remarks;

    // Section 14 reference-data fields: stable codes only, labels resolved
    // client-side. Distinct from the free-text city/province/country above,
    // which are the pre-existing historical fields (see V8 migration note).
    private String countryCode;
    private String regionCode;
    private String cityCode;
    private String description;
    private List<String> images;
    private String status;
}
