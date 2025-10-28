package com.sss.app.dto.library.inclusionexclusion;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class InclusionExclusionCreateRequestDto extends InclusionExclusionDto {
    @Override
    @NotBlank(message = "Name is required")
    public String getName() {
        return super.getName();
    }
}
