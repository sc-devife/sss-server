package com.sss.app.dto.library.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sss.app.dto.library.destination.DestinationResponseDTO;
import com.sss.app.dto.library.location.LocationResponseDTO;
import com.sss.app.dto.library.mealplan.MealPlanResponseDTO;
import com.sss.app.dto.library.roomtype.RoomTypeResponseDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Data
public class HotelResponseDTO {

    private UUID uid;

    private String name;

    private Integer stars;

    // ✅ Full nested objects
    private LocationResponseDTO location;

    private Set<DestinationResponseDTO> destinations;

    private Set<MealPlanResponseDTO> mealPlans;

    private Set<RoomTypeResponseDTO> roomTypes;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    private String childAgeForExtraBed;

    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
