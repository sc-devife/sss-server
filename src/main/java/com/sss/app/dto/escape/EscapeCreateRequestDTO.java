package com.sss.app.dto.escape;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EscapeCreateRequestDTO extends EscapeDTO {
    @NotNull
    private Long leadId;
    private Long sourceId;
    @NotEmpty
    private java.util.List<Long> travellerIds;

    @NotEmpty
    private java.util.List<Long> escapePointIds;

    @NotNull
    private Integer numberOfDays;
}
