package com.sss.app.dto.library.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
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

    // Repeatable "Room Type + Price/Night" rows — null leaves room types
    // untouched (n/a on create, since a brand-new hotel starts with none),
    // an empty list clears them, matching every other relation's
    // partial-update semantics in HotelHelper.applyRelations.
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

    private String address;

    private String contactInfo;

    private String phoneNumber;

    @Email(message = "Invalid email")
    private String email;

    private List<String> images;

    private List<String> amenities;

    private String status;

    private String notes;

    private String accountHolderName;

    private String bankName;

    private String branchName;

    private String accountType;

    private String accountNumber;

    private String ifsc;

    private String upiId;
}
