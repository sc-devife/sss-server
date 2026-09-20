package com.sss.app.dto.library.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sss.app.dto.library.service.ServiceResponseDTO;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.location.LocationResponseDTO;
import com.sss.app.dto.library.mealplan.MealPlanResponseDTO;
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

    private List<HotelRoomTypeResponseDTO> roomTypes;

    private Set<ServiceResponseDTO> services;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    private String childAgeForExtraBed;

    private java.math.BigDecimal basePrice;

    private java.time.LocalDate rateValidFrom;

    private java.time.LocalDate rateValidTo;

    private Boolean isActive;

    private String address;

    private String contactInfo;

    private String phoneNumber;

    private String email;

    private List<String> images;

    // The manually-chosen main image — used wherever a single representative
    // image is needed instead of assuming images[0].
    private String priorityImage;

    private List<String> amenities;

    private String status;

    private String notes;

    private String about;

    private String rulesAndPolicies;

    private String accountHolderName;

    private String bankName;

    private String branchName;

    private String accountType;

    private String accountNumber;

    private String ifsc;

    private String upiId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
