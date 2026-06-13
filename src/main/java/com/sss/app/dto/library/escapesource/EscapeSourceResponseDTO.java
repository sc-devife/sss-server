package com.sss.app.dto.library.escapesource;

import com.sss.app.util.escapeSource.EscapeSourceStatus;
import com.sss.app.util.escapeSource.EscapeSourceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EscapeSourceResponseDTO {
    private Long seqp;
    private EscapeSourceType sourceType;

    private String fullName;
    private String shortName;
    private String tripSourceTag;

    private EscapeSourceStatus status;

    private EscapeSourceB2BDetailsDTO b2bDetails;
}
