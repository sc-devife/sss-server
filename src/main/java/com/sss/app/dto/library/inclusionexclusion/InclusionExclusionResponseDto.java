package com.sss.app.dto.library.inclusionexclusion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class InclusionExclusionResponseDto extends InclusionExclusionDto {
    private Long seqp;
    private String uid;
    private Boolean is_active;
}
