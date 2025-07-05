package com.sss.app.controller;

import com.sss.app.dto.ForgotPasswordRequest;
import com.sss.app.dto.ResetPasswordRequest;
import com.sss.app.entity.UserSession;
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
    private UserSessionRepository userSessionRepo;
    @Autowired
    private UserService userService;
    @Autowired
    JwtValidator jwtValidator;

    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginDetails) throws Exception {

        String username = loginDetails.get("email");
        String password = loginDetails.get("password");

        String token = "";
        try {
            token = authServices.authenticateAndGenerateToken(username, password);
            UserSession userSession = new UserSession(username, token, LocalDateTime.now());
            userSessionRepo.save(userSession);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        if(StringUtils.hasText(token)) {
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = JwtValidator.extractUsername(token);
            userSessionRepo.deleteById(username); // Remove session from DB
            return ResponseEntity.ok("Logged out");
        }
        return ResponseEntity.badRequest().body("Missing token");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            userService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok("Reset link sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid Request");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getEmail(), request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid Request");
        }
    }
}


