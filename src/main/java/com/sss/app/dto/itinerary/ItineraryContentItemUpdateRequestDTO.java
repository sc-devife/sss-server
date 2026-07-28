package com.sss.app.dto.itinerary;

import lombok.Data;

/** Edits the itinerary's own copy — never touches the source library item. */
@Data
public class ItineraryContentItemUpdateRequestDTO {
    private String name;
    private String contentHtml;
}
