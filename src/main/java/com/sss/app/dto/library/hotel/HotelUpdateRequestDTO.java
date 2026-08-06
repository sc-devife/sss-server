package com.sss.app.dto.library.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class HotelUpdateRequestDTO {

    private String name;

    private Integer stars;

    private UUID locationId;

    private String escapePointId;

    private Set<String> escapePointIds;

    private Set<UUID> mealPlanIds;

    private Set<UUID> roomTypeIds;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    private String childAgeForExtraBed;

    private Boolean isActive;

    private String address;

    private String contactInfo;

    private List<String> images;

    private List<String> amenities;

    private String status;
}
