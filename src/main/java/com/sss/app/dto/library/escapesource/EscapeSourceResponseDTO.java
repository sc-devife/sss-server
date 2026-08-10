package com.sss.app.dto.library.escapesource;

import com.sss.app.util.escapeSource.EscapeSourceStatus;
import com.sss.app.util.escapeSource.EscapeSourceType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EscapeSourceResponseDTO {
    private UUID uid;
    private EscapeSourceType sourceType;

    private String fullName;
    private String shortName;
    private String escapeSourceTag;

    private EscapeSourceStatus status;

    private EscapeSourceB2BDetailsDTO b2bDetails;
}
