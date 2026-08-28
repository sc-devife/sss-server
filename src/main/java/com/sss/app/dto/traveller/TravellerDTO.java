package com.sss.app.dto.traveller;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
@Data
public class TravellerDTO {
    // ADULT / CHILD / INFANT — optional (a lot of existing rows predate this
    // field being wired up), but must be one of the three values if given.
    @Pattern(regexp = "^$|ADULT|CHILD|INFANT", message = "Type must be ADULT, CHILD, or INFANT")
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
    private Integer age;
    private String nationality;

    private String passportNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate passportExpiry;

    private String passportIssuingCountry;
}
