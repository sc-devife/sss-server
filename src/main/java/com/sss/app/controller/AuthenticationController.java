package com.sss.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sss.app.dto.ForgotPasswordRequest;
import com.sss.app.dto.ResetPasswordRequest;
import com.sss.app.entity.UserSession;
import com.sss.app.jwtToken.JwtValidator;
import com.sss.app.repository.UserSessionRepository;
import com.sss.app.service.AuthenticationService;
import com.sss.app.service.TestService;
import com.sss.app.service.UserService;
import org.json.JSONArray;
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
    TestService testSer;
    @Autowired
    private UserService userService;
    @Autowired
    JwtValidator jwtValidator;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        System.out.println("Ping endpoint hit!");
        return ResponseEntity.ok("pong");
    }
    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginDetails) throws Exception {

        String username = loginDetails.get("email");
        String password = loginDetails.get("password");

        String token = "";
        try {
            token = authServices.authenticateAndGenerateToken(username, password);
            System.out.println("Token username: " + username);
            System.out.println("Token LocalDateTime: " + LocalDateTime.now());
            UserSession userSession = new UserSession(username, token, LocalDateTime.now());
            System.out.println("UserSession created: " + userSession);
            userSessionRepo.save(userSession);
            System.out.println("UserSession After created: " + userSession);
            //userSessionRepo.save(new UserSession(username, token, LocalDateTime.now()));
        } catch (Exception e) {
            // Handle exception, e.g., log it and return an error response
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        if(StringUtils.hasText(token)) {
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @RequestMapping(value = "/hello", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> hello() throws JsonProcessingException {
        JSONArray outContent;
        outContent = testSer.getGreeting();
        return ResponseEntity.ok(outContent.toString());
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
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("Test endpoint hit");
        return ResponseEntity.ok("Test successful");
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        System.out.println("Forgot password start email === " + request.getEmail());
        userService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok("Reset link sent to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        System.out.println("start reset pass ===");
        System.out.println("start reset new token ===" + request.getToken());
        System.out.println("start reset new pass ===" + request.getNewPassword());
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully.");
    }
}
