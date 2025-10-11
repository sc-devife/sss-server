package com.sss.app.dto.library.escapepoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class EscapePointResponseDto extends EscapePointDto {
    private Long seqp;
    private String uid;
}
