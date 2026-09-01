package com.sss.app.dto.itinerary;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class HotelDetailDTO {

    private UUID mealPlanId;

    private UUID roomTypeId;

    private Integer paxPerRoom;

    private Integer roomCount;

    private Integer adultsWithExtraBed;

    private Integer childrenWithExtraBed;

    private Integer childrenNoBed;

    private Integer complimentaryChildCount;

    private BigDecimal price;

    private BigDecimal totalPrice;

    private List<HotelInclusionDTO> inclusions;
}
