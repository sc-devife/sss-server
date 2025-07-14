package com.sss.app.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class UserUpdateRequestDto extends UserDto {
    @Override
    @NotBlank(message = "First Name is required")
    public String getFirst_name() {
        return super.getFirst_name();
    }

    @Override
    @NotBlank(message = "Last Name is required")
    public String getLast_name() {
        return super.getLast_name();
    }
}
