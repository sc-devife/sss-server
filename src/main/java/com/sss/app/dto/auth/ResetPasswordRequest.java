package com.sss.app.dto.auth;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String token;
    private String newPassword;
}
