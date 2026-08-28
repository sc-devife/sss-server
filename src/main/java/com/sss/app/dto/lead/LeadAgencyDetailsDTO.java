package com.sss.app.dto.lead;

import lombok.Data;

@Data
public class LeadAgencyDetailsDTO {
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String streetAddress;
    private String locality;
    private String landmark;
    private String billingName;
    private String additionalBillingDetails;
}
