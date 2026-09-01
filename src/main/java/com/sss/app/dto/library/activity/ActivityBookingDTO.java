package com.sss.app.dto.library.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityBookingDTO {

    private UUID itineraryItemUid;

    private UUID escapeUid;

    private String escapeStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate escapeStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate escapeEndDate;

    private String leadName;

    private Integer dayNumber;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    private String notes;
}
