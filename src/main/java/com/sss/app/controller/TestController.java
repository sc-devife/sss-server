package com.sss.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sss.app.entity.InvitationToken;
import com.sss.app.repository.InvitationTokenRepository;
import com.sss.app.service.InvitationService;
import com.sss.app.service.TestService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    TestService testSer;

    @RequestMapping(value = "/hello", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> hello() throws JsonProcessingException {
        JSONArray outContent;
        outContent = testSer.getGreeting();

        return ResponseEntity.ok(outContent.toString());

    }

    @Autowired
    private UserEmailController emailController;

    @RequestMapping(value = "/send", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String sendEmail(@RequestBody Map<String, String> emailData){
        String to = emailData.get("email");
        String subject = emailData.get("subject");
        String body = emailData.get("body");

        emailController.sendSimpleEmail(to, subject, body);
        return "Email sent successfully! FROM EmailHandle";
    }

    @Autowired
    private InvitationService invitationService;

    @PostMapping("/invite")
    public ResponseEntity<String> inviteTest(@RequestParam String email) {
        System.out.println("Calling invite method with email: " + email);
        invitationService.inviteUser(email);
        return ResponseEntity.ok("Invitation sent!");
    }

    @Autowired
    private InvitationTokenRepository tokenRepository;
    @GetMapping("/redirect")
    public String showRegistrationForm(@RequestParam("id") String token)
    {
        Optional<InvitationToken> optional = tokenRepository.findByToken(token);
        if (optional.isEmpty() || optional.get().isUsed() || optional.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            return "error";
        }
        return "URL redirect to registration form with token: " + token;
    }

    @PostMapping("/register")
    public String handleRegistration(@RequestParam("token") String token,
                                     @ModelAttribute("userDto") UserRegistrationDto userDto,
                                     Model model) {
        InvitationToken invitation = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));


        if (invitation.isUsed() || invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Token expired or already used.");
            return "error";
        }

        System.out.println("Creating user: " + userDto.getName() + ", Email: " + invitation.email);

        invitation.setUsed(true);
        tokenRepository.save(invitation);

        return "success";
    }
}
