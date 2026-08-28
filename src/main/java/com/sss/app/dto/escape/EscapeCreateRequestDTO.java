package com.sss.app.dto.escape;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class EscapeCreateRequestDTO extends EscapeDTO {
    @NotNull
    private UUID leadUid;
    @NotEmpty
    private java.util.List<UUID> travellerUids;

    // EscapePoint's own uid is String-typed (not UUID — see EscapePoint
    // entity/EscapePointRepository.findByUid(String)), so this stays String
    // to match rather than forcing a UUID that would never resolve.
    @NotEmpty
    private java.util.List<String> escapePointUids;

    @NotNull
    private Integer numberOfDays;
}
