package com.sss.app.service;

import com.sss.app.dto.auth.ForgotPasswordRequest;
import com.sss.app.dto.auth.ResetPasswordRequest;
import com.sss.app.entity.UserCredential;
import com.sss.app.entity.auth.PasswordResetToken;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.helper.UserCredentialsHelper;
import com.sss.app.helper.UsersHelper;
import com.sss.app.repository.PasswordResetTokenRepository;
import com.sss.app.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final long RESET_TOKEN_VALIDITY_HOURS = 1;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private final UsersHelper usersHelper;
    @Autowired
    private final UserCredentialsHelper userCredentialsHelper;

    @Autowired
    private final UserCredentialRepository userCredentialRepository;

    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Silently no-ops if the email isn't registered — the caller (forgot-password
     * endpoint) always returns the same generic response either way, so this
     * can't be used to enumerate which emails have accounts.
     */
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        User user;
        try {
            user = usersHelper.getUserByEmail(request.getEmail());
        } catch (Exception e) {
            return;
        }

        // email is unique on this table — reuse the existing row for repeat
        // requests instead of inserting a second one (which would violate that
        // constraint), so someone can request a new link as many times as needed.
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByEmail(user.getEmail())
                .orElseGet(PasswordResetToken::new);

        String token = UUID.randomUUID().toString();
        passwordResetToken.setEmail(user.getEmail());
        passwordResetToken.setResetToken(token);
        passwordResetToken.setTokenExpiry(LocalDateTime.now().plusHours(RESET_TOKEN_VALIDITY_HOURS));
        passwordResetTokenRepository.save(passwordResetToken);

        sendEmail(passwordResetToken.getEmail(), token);
    }

    private void sendEmail(String email, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token + "&email=" + email;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Reset your password");
        mailMessage.setText("Click this link to reset your password: " + resetUrl
                + "\n\nThis link expires in " + RESET_TOKEN_VALIDITY_HOURS + " hour.");

        mailSender.send(mailMessage);
    }

    public void resetPassword(ResetPasswordRequest resetPasswordDto) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByResetToken(resetPasswordDto.getToken())
                .orElseThrow(() -> new ConflictException("Invalid or already-used reset link"));

        if (!passwordResetToken.getEmail().equalsIgnoreCase(resetPasswordDto.getEmail())) {
            throw new ConflictException("Invalid or already-used reset link");
        }

        if (passwordResetToken.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ConflictException("This reset link has expired");
        }

        User user = usersHelper.getUserByEmail(resetPasswordDto.getEmail());
        UserCredential userCredential = userCredentialsHelper.getUserCredentialByUserSeqp(user.getSeqp());

        userCredential.setPassword_hash(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        userCredential.registerSuccessfulLogin();
        userCredentialRepository.save(userCredential);

        // Burn the token so the link can't be reused.
        passwordResetToken.setResetToken("");
        passwordResetToken.setTokenExpiry(LocalDateTime.of(1970, 1, 1, 0, 0));
        passwordResetTokenRepository.save(passwordResetToken);
    }
}
