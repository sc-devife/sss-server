package com.sss.app.dto.library.hotel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

// One row of the Add/Edit Hotel popup's repeatable "Room Types" section —
// a RoomType selection paired with this hotel's own price/night for it.
@Data
public class HotelRoomTypePricingRequestDTO {

    @NotNull(message = "Room type is required")
    private UUID roomTypeId;

    private BigDecimal price;
}
