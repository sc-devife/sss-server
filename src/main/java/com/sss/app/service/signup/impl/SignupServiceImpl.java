package com.sss.app.service.signup.impl;

import com.sss.app.dto.signup.SignupCreateRequestDTO;
import com.sss.app.dto.signup.SignupResponseDTO;
import com.sss.app.helper.signup.SignupHelper;
import com.sss.app.mapper.signup.SignupMapper;
import com.sss.app.service.signup.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {

    private final SignupHelper signupHelper;
    private final SignupMapper signupMapper;

    @Override
    public SignupResponseDTO createSignup(SignupCreateRequestDTO request) {
        return signupMapper.toResponse(signupHelper.createSignup(request));
    }
}
