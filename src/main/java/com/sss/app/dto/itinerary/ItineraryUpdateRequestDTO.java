package com.sss.app.dto.itinerary;

import lombok.Data;

@Data
public class ItineraryUpdateRequestDTO {
    private String name;
    private String status; // draft / active / superseded
}
