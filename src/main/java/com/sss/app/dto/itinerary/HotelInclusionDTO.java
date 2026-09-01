package com.sss.app.dto.itinerary;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class HotelInclusionDTO {

    private String service;

    private LocalTime startTime;

    private Integer durationMinutes;

    private BigDecimal totalPrice;

    private String comments;
}
