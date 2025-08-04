package com.sss.app.service;

import com.sss.app.dto.auth.ForgotPasswordRequest;
import com.sss.app.dto.auth.ResetPasswordRequest;
import com.sss.app.entity.UserCredential;
import com.sss.app.entity.auth.PasswordResetToken;
import com.sss.app.entity.users.User;
import com.sss.app.helper.UserCredentialsHelper;
import com.sss.app.helper.UsersHelper;
import com.sss.app.repository.PasswordResetTokenRepository;
import com.sss.app.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private final UsersHelper usersHelper;
    @Autowired
    private final UserCredentialsHelper userCredentialsHelper;

    @Autowired
    private final UserCredentialRepository userCredentialRepository;

    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;

    public void initiatePasswordReset(ForgotPasswordRequest request) {
        User user = usersHelper.getUserByEmail(request.getEmail());

        UserCredential userCredential = userCredentialsHelper.getUserCredentialBySeqa(user.getSeqp());

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        String token = UUID.randomUUID().toString();
        passwordResetToken.setEmail(user.getEmail());//resetToken
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

    public void resetPassword(ResetPasswordRequest resetPasswordDto) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByResetToken(resetPasswordDto.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        User user = usersHelper.getUserByEmail(resetPasswordDto.getEmail());

        UserCredential userCredential = userCredentialsHelper.getUserCredentialBySeqa(user.getSeqp());

        if (passwordResetToken.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        userCredential.setPassword_hash(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        passwordResetToken.setResetToken(""); //Used data
        passwordResetToken.setTokenExpiry(LocalDateTime.of(1970, 1, 1, 0, 0)); //Used data
        passwordResetTokenRepository.save(passwordResetToken);
        userCredentialRepository.save(userCredential);
    }
}
