package com.sss.app.controller.signup;

import com.sss.app.dto.signup.SignupCreateRequestDTO;
import com.sss.app.dto.signup.SignupResponseDTO;
import com.sss.app.service.signup.SignupService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signup")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignupResponseDTO> createSignup(@Valid @RequestBody SignupCreateRequestDTO payload) {
        return ResponseEntity.ok(signupService.createSignup(payload));
    }
}
