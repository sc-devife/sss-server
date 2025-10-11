package com.sss.app.dto.library.escapepoint;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class EscapePointUpdateRequestDto extends EscapePointDto {
    @Override
    @NotBlank(message = "Id is required")
    public String getId() {
        return super.getId();
    }

    @Override
    @NotBlank(message = "Name is required")
    public String getName() {
        return super.getName();
    }
}
