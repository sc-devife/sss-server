package com.sss.app.dto.lead;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
@Data
public class LeadDTO {
    private String name;
    private String email;

    // Optional field — empty is valid, but if provided must be a syntactically
    // valid E.164 number (the frontend's PhoneInput always submits this shape;
    // real per-country length/format rules are enforced there via
    // libphonenumber-js, this is a defense-in-depth shape guard only).
    @Pattern(regexp = "^$|^\\+[1-9]\\d{6,14}$", message = "Enter a valid phone number")
    private String phone;
    private String destination;
    private Integer numberOfPeople;
    private LocalDate travelDate;
    private Integer durationDays;
    private Double budget;
    private String status;
    private String sourceCode;
    private String sourceRefId;
    private String escapePointId; // EscapePoint uid, resolved manually (see LeadsHelper)
    private Boolean isPriority;
    private String originCity;
    private String travelType;
    private String notes;
}
