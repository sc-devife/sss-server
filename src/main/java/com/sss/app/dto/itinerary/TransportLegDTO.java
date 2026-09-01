package com.sss.app.dto.itinerary;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransportLegDTO {

    private Integer legOrder;

    private String direction; // onward / return / null

    private String departureAirport;

    private String departureTerminal;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureTime;

    private String arrivalAirport;

    private String arrivalTerminal;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivalTime;

    private String flightNumber;
}
