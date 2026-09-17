package com.sss.app.dto.itinerary;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Change/Replace Hotel flow — drops the existing hotel item and creates its
 * replacement in one transactional operation, linking the two. See
 * ItineraryItemHelper.replaceHotel.
 */
@Data
public class ItineraryItemReplaceRequestDTO {

    // Required — same "Drop requires a reason" rule as a plain hotel Drop.
    private String droppingReason;

    // Optional — same as a plain hotel Drop.
    private BigDecimal cancellationCharge;

    // The replacement hotel, in the exact same shape a normal "Add Hotel"
    // would submit. itineraryUid/itemType are ignored (forced from the item
    // being replaced, in ItineraryItemHelper.replaceHotel, before this is
    // handed to create()) — present on ItineraryItemCreateRequestDTO only
    // because create() and replace() share the same shape. Deliberately NOT
    // @Valid-cascaded: itineraryUid's own @NotNull would reject this before
    // replaceHotel() gets a chance to fill it in.
    @NotNull(message = "newHotel is required")
    private ItineraryItemCreateRequestDTO newHotel;
}
