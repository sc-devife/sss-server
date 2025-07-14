package com.sss.app.service;

import com.sss.app.entity.auth.PasswordResetToken;
import com.sss.app.entity.UserAccount;
import com.sss.app.repository.PasswordResetTokenRepository;
import com.sss.app.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserAccountRepository userAccountRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;

    public void initiatePasswordReset(String email) {
        UserAccount userAccount = userAccountRepository.findByUserName(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

       PasswordResetToken passwordResetToken = new PasswordResetToken();
        String token = UUID.randomUUID().toString();
        passwordResetToken.setEmail(email);//resetToken
        passwordResetToken.setResetToken(token);//resetToken
        passwordResetToken.setTokenExpiry(LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(passwordResetToken);

        sendEmail(passwordResetToken.getEmail(), token);
    }

    private void sendEmail(String email, String token) {
        String resetUrl = "http://localhost:8080/sss/api/login/reset-password?token=" + token;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Reset your password");
        mailMessage.setText("Click this link to reset your password: " + resetUrl);

        mailSender.send(mailMessage);
    }

    public void resetPassword(String email, String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        UserAccount userAccount = userAccountRepository.findByUserName(email)
                .orElseThrow(() -> new RuntimeException("User ot found with this email"));
        if (passwordResetToken.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        userAccount.setPasswordHash(passwordEncoder.encode(newPassword));
        passwordResetToken.setResetToken(""); //Used data
        passwordResetToken.setTokenExpiry(LocalDateTime.of(1970, 1, 1, 0, 0)); //Used data
        passwordResetTokenRepository.save(passwordResetToken);
        userAccountRepository.save(userAccount);
    }
}
