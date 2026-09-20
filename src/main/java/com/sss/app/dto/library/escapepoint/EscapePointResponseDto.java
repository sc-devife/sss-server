package com.sss.app.dto.library.escapepoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class EscapePointResponseDto extends EscapePointDto {
    private Long seqp;
    private String uid;

    // The Escape Point's manually-chosen main image — used wherever a single
    // representative image is needed (Escape Summary, Dashboard cards,
    // Quotation data), instead of assuming images[0].
    private String priorityImage;

    // Resolved from EscapePointLocation — see EscapePointsServiceImpl.
    private List<EscapePointLocationRefDto> locations;

    // "City, State, Country" of the primary location (or the first linked
    // location if none is marked primary yet) — same composed shape the old
    // countryCode/regionCode/cityCode resolution produced, now computed
    // server-side from real data instead of reference-data code lookups.
    private String locationLabel;

    // How many (non-archived) hotels / activities are linked to this Escape
    // Point — shown as columns on the Escape Points list. Only filled in by
    // EscapePointsServiceImpl; null on Escape Points nested in other responses.
    private Integer hotelCount;

    private Integer activityCount;
}
