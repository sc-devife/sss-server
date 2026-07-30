package com.sss.app.dto.users.invitations;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class InviteUserRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotEmpty(message = "Select at least one role")
    private List<String> roles;
}
