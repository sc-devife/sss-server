package com.sss.app.entity.lead;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Billing/contact details for a lead sourced through a B2B agency partner
// (Lead.sourceType = AGENCY) — same shape as the old EscapeSourceB2BDetails,
// relocated here since the source relationship belongs to the Lead, not the
// Escape it may later become (see V62/V63 migration notes).
@Entity
@Table(name = "lead_agency_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadAgencyDetails {

    @Id
    @Column(name = "lead_id")
    private Long leadId;

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

    @Column(columnDefinition = "TEXT")
    private String additionalBillingDetails;
}
