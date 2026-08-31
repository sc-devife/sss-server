package com.sss.app.dto.library.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sss.app.dto.library.activity.ActivityResponseDTO;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.location.LocationResponseDTO;
import com.sss.app.dto.library.mealplan.MealPlanResponseDTO;
import com.sss.app.dto.library.roomtype.RoomTypeResponseDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class HotelResponseDTO {

    private UUID uid;

    private String name;

    private Integer stars;

    // ✅ Full nested objects
    private LocationResponseDTO location;

    private EscapePointResponseDto escapePoint;

    private Set<EscapePointResponseDto> escapePoints;

    private Set<MealPlanResponseDTO> mealPlans;

    private Set<RoomTypeResponseDTO> roomTypes;

    private Set<ActivityResponseDTO> activities;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    private String childAgeForExtraBed;

    private java.time.LocalDate rateValidFrom;

    private java.time.LocalDate rateValidTo;

    private Boolean isActive;

    private String address;

    private String contactInfo;

    private List<String> images;

    private List<String> amenities;

    private String status;

    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
