package com.sss.app.dto.traveller;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private String age;
    private String nationality;
}
