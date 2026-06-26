package com.sss.app.dto.library.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Data
public class HotelCreateRequestDTO {

    @NotBlank(message = "Name field is required")
    private String name;

    private Integer stars;

    @NotNull(message = "Location is required")
    private UUID locationId;

    private Set<UUID> destinationIds;

    private Set<UUID> mealPlanIds;

    private Set<UUID> roomTypeIds;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    private String childAgeForExtraBed;
}
