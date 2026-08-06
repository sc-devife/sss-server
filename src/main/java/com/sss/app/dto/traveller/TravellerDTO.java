package com.sss.app.dto.traveller;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
@Data
public class TravellerDTO {
    private String type;
    private String salutation;

   @NotBlank(message = "First name is mandatory")
   private String firstName;

    private String lastName;

    @Email(message = "Invalid email")
    private String email;

    // Optional field — empty is valid, but if provided must be a syntactically
    // valid E.164 number (the frontend's PhoneInput always submits this shape;
    // real per-country length/format rules are enforced there via
    // libphonenumber-js, this is a defense-in-depth shape guard only).
    @Pattern(regexp = "^$|^\\+[1-9]\\d{6,14}$", message = "Enter a valid phone number")
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private String age;
    private String nationality;
}
