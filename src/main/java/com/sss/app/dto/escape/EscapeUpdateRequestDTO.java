package com.sss.app.dto.escape;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EscapeUpdateRequestDTO extends EscapeDTO {

  //  @NotNull(message = "Trip ID is required")
    //private Long seqp;

    @NotNull(message = "Lead ID is required")
    private Long leadId;
    private Long sourceId;

    private List<Long> travellerIds;

    private List<Long> destinationIds;
}
