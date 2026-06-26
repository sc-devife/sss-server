package com.sss.app.helper.signup;

import com.sss.app.dto.signup.SignupCreateRequestDTO;
import com.sss.app.entity.signup.Signup;
import com.sss.app.exception.ConflictException;
import com.sss.app.mapper.signup.SignupMapper;
import com.sss.app.repository.signup.SignupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SignupHelper {

    private static final long SIGNUP_RETRY_LIMIT_HOURS = 24;

    private final SignupRepository signupRepository;
    private final SignupMapper signupMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public Signup createSignup(SignupCreateRequestDTO payload) {
        if (signupRepository.existsByUserId(payload.getUserId())) {
            throw new ConflictException("User ID is already registered");
        }

        if (signupRepository.existsByEmail(payload.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        signupRepository.findFirstByEmailOrMobileNumberOrderByCreatedAtDesc(payload.getEmail(), payload.getMobileNumber())
                .filter(previous -> previous.getCreatedAt().isAfter(LocalDateTime.now().minusHours(SIGNUP_RETRY_LIMIT_HOURS)))
                .ifPresent(previous -> {
                    throw new ConflictException("A signup with this email or mobile number was already attempted in the last 24 hours");
                });

        Signup signup = signupMapper.toEntity(payload);
        signup.setUid(UUID.randomUUID().toString());
        signup.setPasswordHash(passwordEncoder.encode(payload.getPassword()));
        signup.setCreatedAt(LocalDateTime.now());

        return signupRepository.save(signup);
    }
}
