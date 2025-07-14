package com.sss.app.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserCreateRequestDto extends UserDto {
    @Override
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    public String getEmail() {
        return super.getEmail();
    }

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
