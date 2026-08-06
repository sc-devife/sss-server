package com.sss.app.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserDto {
    @NotBlank(groups = Create.class)
    private String email;

    @NotBlank(groups = Create.class)
    private String first_name;

    @NotBlank(groups = Create.class)
    private String last_name;

    // Optional field — empty is valid, but if provided must be a syntactically
    // valid E.164 number (the frontend's PhoneInput always submits this shape;
    // real per-country length/format rules are enforced there via
    // libphonenumber-js, this is a defense-in-depth shape guard only).
    @Pattern(regexp = "^$|^\\+[1-9]\\d{6,14}$", message = "Enter a valid phone number")
    private String contact_number;

    public interface Create {
    }
}

