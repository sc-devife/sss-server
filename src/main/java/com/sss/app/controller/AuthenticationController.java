package com.sss.app.controller;

import com.sss.app.dto.auth.ForgotPasswordRequest;
import com.sss.app.dto.auth.LoginResponse;
import com.sss.app.dto.auth.ResetPasswordRequest;
import com.sss.app.entity.UserSession;
import com.sss.app.exception.AccountBlockedException;
import com.sss.app.jwtToken.JwtValidator;
import com.sss.app.repository.UserSessionRepository;
import com.sss.app.service.AuthenticationService;
import com.sss.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("api/login")
public class AuthenticationController {
    @Autowired
    AuthenticationService authServices;
    @Autowired
    JwtValidator jwtValidator;
    @Autowired
    private UserSessionRepository userSessionRepo;
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginDetails) throws Exception {

        String username = loginDetails.get("email");
        String password = loginDetails.get("password");

        LoginResponse loginResponse;
        try {
            loginResponse = authServices.authenticateAndGenerateToken(username, password);
            UserSession userSession = new UserSession(username, loginResponse.getToken(), LocalDateTime.now());
            userSessionRepo.save(userSession);
        } catch (AccountBlockedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        if (StringUtils.hasText(loginResponse.getToken())) {
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length());
            String username = JwtValidator.extractUsername(token);
            userSessionRepo.deleteById(username); // Remove session from DB
            return ResponseEntity.ok("Logged out");
        }
        return ResponseEntity.badRequest().body("Missing token");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordDto) {
        // Always the same response whether or not the email is registered —
        // initiatePasswordReset silently no-ops for an unknown email — so this
        // can't be used to enumerate which addresses have accounts.
        userService.initiatePasswordReset(forgotPasswordDto);
        return ResponseEntity.ok("If that email is registered, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordDto) {
        try {
            userService.resetPassword(resetPasswordDto);
            return ResponseEntity.ok("Password reset successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @GetMapping("/generate")
    public String generatePasswordHash(@RequestParam String password) {
        return authServices.generatePasswordHash(password);
    }
}


