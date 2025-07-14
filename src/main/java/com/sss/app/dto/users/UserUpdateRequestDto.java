package com.sss.app.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserUpdateRequestDto extends UserDto {
    @NotBlank(message = "First Name is required")
    private String first_name;

    @NotBlank(message = "Last Name is required")
    private String last_name;
}
