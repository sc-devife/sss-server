package com.sss.app.helper.signup;

import com.sss.app.dto.signup.SignupCreateRequestDTO;
import com.sss.app.entity.UserCredential;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.mapper.signup.SignupMapper;
import com.sss.app.repository.UserCredentialRepository;
import com.sss.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SignupHelper {

    private static final long SIGNUP_RETRY_LIMIT_HOURS = 24;

    private final SignupMapper signupMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;

    @Transactional
    public User createSignup(SignupCreateRequestDTO payload) {
        if (userRepository.existsByUserId(payload.getUserId())) {
            throw new ConflictException("User ID is already registered");
        }

        if (userRepository.existsByEmail(payload.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        List<User> previousSignups = userRepository.findByEmailOrContactNumberOrderByCreatedAtDesc(
                payload.getEmail(), payload.getMobileNumber());

        if (!previousSignups.isEmpty()
                && previousSignups.get(0).getCreatedAt() != null
                && previousSignups.get(0).getCreatedAt().isAfter(LocalDateTime.now().minusHours(SIGNUP_RETRY_LIMIT_HOURS))) {
            throw new ConflictException("A signup with this email or mobile number was already attempted in the last 24 hours");
        }

        String passwordHash = passwordEncoder.encode(payload.getPassword());

        User user = signupMapper.toEntity(payload);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        UserCredential userCredential = UserCredential.create(user.getSeqp(), passwordHash);
        userCredentialRepository.save(userCredential);

        return user;
    }
}
