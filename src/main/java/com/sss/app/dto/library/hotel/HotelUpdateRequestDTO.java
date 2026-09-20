package com.sss.app.dto.library.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
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

    // Repeatable "Room Type + Price/Night" rows — null leaves room types
    // untouched, an empty list clears them (see HotelCreateRequestDTO).
    private List<HotelRoomTypePricingRequestDTO> roomTypePricing;

    private Set<UUID> serviceIds;

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

    @Email(message = "Invalid email")
    private String email;

    private List<String> images;

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
}
