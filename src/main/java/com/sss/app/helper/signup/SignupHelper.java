package com.sss.app.helper.signup;

import com.sss.app.dto.signup.SignupCreateRequestDTO;
import com.sss.app.entity.UserCredential;
import com.sss.app.entity.signup.Signup;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.mapper.signup.SignupMapper;
import com.sss.app.repository.UserCredentialRepository;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.signup.SignupRepository;
import jakarta.transaction.Transactional;
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
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;

    @Transactional
    public Signup createSignup(SignupCreateRequestDTO payload) {
        if (signupRepository.existsByUserId(payload.getUserId())) {
            throw new ConflictException("User ID is already registered");
        }

        if (signupRepository.existsByEmail(payload.getEmail()) || userRepository.existsByEmail(payload.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        signupRepository.findFirstByEmailOrMobileNumberOrderByCreatedAtDesc(payload.getEmail(), payload.getMobileNumber())
                .filter(previous -> previous.getCreatedAt().isAfter(LocalDateTime.now().minusHours(SIGNUP_RETRY_LIMIT_HOURS)))
                .ifPresent(previous -> {
                    throw new ConflictException("A signup with this email or mobile number was already attempted in the last 24 hours");
                });

        String passwordHash = passwordEncoder.encode(payload.getPassword());

        Signup signup = signupMapper.toEntity(payload);
        signup.setUid(UUID.randomUUID().toString());
        signup.setPasswordHash(passwordHash);
        signup.setCreatedAt(LocalDateTime.now());
        signup = signupRepository.save(signup);

        User user = User.builder()
                .name(payload.getName())
                .email(payload.getEmail())
                .contact_number(payload.getMobileNumber())
                .build();
        user = userRepository.save(user);

        UserCredential userCredential = UserCredential.create(user.getSeqp(), passwordHash);
        userCredentialRepository.save(userCredential);

        return signup;
    }
}
