package com.sss.app.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.@$!%*#?&])[A-Za-z\\d.@$!%*#?&]{8,}$",
            message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;
}
