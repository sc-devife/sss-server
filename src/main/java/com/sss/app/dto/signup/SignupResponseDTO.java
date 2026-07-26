package com.sss.app.dto.signup;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignupResponseDTO {
    private String uid;
    private String name;
    private String first_name;
    private String last_name;
    private String userId;
    private String email;
    private String mobileNumber;
    private LocalDateTime createdAt;
}
