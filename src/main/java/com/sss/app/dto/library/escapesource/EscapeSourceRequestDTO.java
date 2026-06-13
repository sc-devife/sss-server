package com.sss.app.dto.library.escapesource;

import com.sss.app.util.escapeSource.EscapeSourceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EscapeSourceRequestDTO {

    private EscapeSourceType sourceType;

    private String fullName;
    private String shortName;

    private String tripSourceTag;

    // Only for B2B
    private EscapeSourceB2BDetailsDTO b2bDetails;
}
