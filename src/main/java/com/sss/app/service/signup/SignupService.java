package com.sss.app.service.signup;

import com.sss.app.dto.signup.SignupCreateRequestDTO;
import com.sss.app.dto.signup.SignupResponseDTO;

public interface SignupService {
    SignupResponseDTO createSignup(SignupCreateRequestDTO request);
}
