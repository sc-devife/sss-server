package com.sss.app.dto.library.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class HotelCreateRequestDTO {

    @NotBlank(message = "Name field is required")
    private String name;

    private Integer stars;

    @NotNull(message = "Location is required")
    private UUID locationId;

    // Dictionary-aligned single escape point (see Hotel.escapePoint) — the uid
    // of an EscapePoint, resolved to the entity in HotelHelper like locationId.
    private String escapePointId;

    private Set<String> escapePointIds;

    private Set<UUID> mealPlanIds;

    private Set<UUID> roomTypeIds;

    private Set<UUID> activityIds;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    private String childAgeForExtraBed;

    private java.time.LocalDate rateValidFrom;

    private java.time.LocalDate rateValidTo;

    private String address;

    private String contactInfo;

    private List<String> images;

    private List<String> amenities;

    private String status;

    private String notes;
}
