package com.sss.app.dto.library.inclusionexclusion;

import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class InclusionExclusionResponseDto extends InclusionExclusionDto {
    private Long seqp;
    private String uid;
    private Boolean isActive;
    private EscapePointResponseDto destination;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
