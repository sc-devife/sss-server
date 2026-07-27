package com.sss.app.dto.lead;

import lombok.Data;

import java.time.LocalDate;
@Data
public class LeadDTO {
    private String name;
    private String email;
    private String phone;
    private String destination;
    private Integer numberOfPeople;
    private LocalDate travelDate;
    private Integer durationDays;
    private Double budget;
    private String status;
    private String sourceCode;
    private String sourceRefId;
    private String destinationId; // EscapePoint uid, resolved manually (see LeadsHelper)
    private Boolean isPriority;
    private String notes;
}
