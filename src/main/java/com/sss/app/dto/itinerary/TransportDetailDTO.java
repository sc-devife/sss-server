package com.sss.app.dto.itinerary;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TransportDetailDTO {

    private String modeCode;

    private String vehicleTypeCode;

    private BigDecimal price;

    private String tripType; // one_way / round_trip / multi_city

    private BigDecimal costPrice;

    private Boolean costPricePerPerson;

    private BigDecimal sellingPrice;

    private Boolean sellingPricePerPerson;

    private Integer adultsCount;

    private Integer childrenCount;

    private Integer infantsCount;

    private String additionalOptions;

    private List<TransportLegDTO> legs;
}
