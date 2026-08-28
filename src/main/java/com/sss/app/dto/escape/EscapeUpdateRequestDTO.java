package com.sss.app.dto.escape;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class EscapeUpdateRequestDTO extends EscapeDTO {

  //  @NotNull(message = "Escape ID is required")
    //private Long seqp;

    @NotNull(message = "Lead ID is required")
    private UUID leadUid;

    private List<UUID> travellerUids;

    // EscapePoint's own uid is String-typed — see note in
    // EscapeCreateRequestDTO.
    private List<String> escapePointUids;
}
