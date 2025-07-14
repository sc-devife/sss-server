package com.sss.app.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    @NotBlank(groups = Create.class)
    private String email;

    @NotBlank(groups = Create.class)
    private String first_name;

    @NotBlank(groups = Create.class)
    private String last_name;

    private String contact_number;

    public interface Create {
    }
}

