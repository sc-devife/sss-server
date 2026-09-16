package com.sss.app.dto.library.hotel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelRoomTypeResponseDTO {

    private UUID roomTypeId;

    private String name;

    private String description;

    private BigDecimal price;
}
