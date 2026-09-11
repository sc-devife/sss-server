package com.sss.app.dto.lead;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
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

    // Today or later only — mirrors the DatePicker's `min` on the frontend.
    // @FutureOrPresent treats null as valid (travelDate is optional), and
    // this is shared by both LeadController's create and update endpoints
    // (both bind LeadCreateRequestDTO, which extends this class), so a past
    // date is rejected the same way whether the lead is being created or
    // edited.
    @FutureOrPresent(message = "Travel date cannot be in the past")
    private LocalDate travelDate;
    private Integer durationNights;
    private Double budget;
    private String status;

    // DIRECT | AGENCY — see Lead.sourceType. sourceChannel only applies when
    // DIRECT; agencyDetails only applies when AGENCY.
    private String sourceType;
    private String sourceChannel;
    private String sourceRefId;
    private LeadAgencyDetailsDTO agencyDetails;

    private List<String> escapePointIds; // EscapePoint uids, resolved manually (see LeadsHelper)
    private Boolean isPriority;
    private String originCity;
    private String travelType;
    private String notes;
}
