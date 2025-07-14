package com.sss.app.controller;

import com.sss.app.dto.users.invitations.UserInvitationDto;
import com.sss.app.service.UserInvitationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/newuser")
public class UserInvitationsController {
    //@Autowired
    //private InvitationEmailController emailController;

/*    @RequestMapping(value = "/send", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String sendEmail(@RequestBody Map<String, String> emailData){
        String to = emailData.get("email");
        String subject = emailData.get("subject");
        String body = emailData.get("body");

        emailController.sendSimpleEmail(to, subject, body);
        return "Email sent successfully! FROM EmailHandle";
    }*/

    // @Autowired
    private UserInvitationsService userInvitationsService;

    public UserInvitationsController(UserInvitationsService userInvitationsService) {
        this.userInvitationsService = userInvitationsService;
    }

    @RequestMapping("/invite")
    public ResponseEntity<UserInvitationDto> inviteUser(@RequestParam String email) {
        return ResponseEntity.ok(userInvitationsService.inviteUser(email));
    }

/*    @Autowired
    private InvitationTokenRepository tokenRepository;
    @GetMapping("/redirect")
    public String showRegistrationForm(@RequestParam("id") String token)
    {
        Optional<UserInvitation> optional = tokenRepository.findByUserInvitation(token);
        if (optional.isEmpty() || optional.get().is_used() || optional.get().getExpires_set().isBefore(LocalDateTime.now())) {
            return "error";
        }
        return "URL redirect to registration form with token: " + token;
    }*/

/*    @PostMapping("/register")
    public String handleRegistration(@RequestParam("token") String token,
                                     @ModelAttribute("userDto") UserRegistrationDto userDto
                                     *//*Model model*//*) {
        UserInvitation invitation = tokenRepository.findByUserInvitation(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));


        if (invitation.is_used() || invitation.getExpires_set().isBefore(LocalDateTime.now())) {
            //model.addAttribute("error", "Token expired or already used.");
            return "error";
        }

        System.out.println("Creating user: " + userDto.getName() *//*+ ", Email: " + invitation.email*//*);

        invitation.set_used(true);
        tokenRepository.save(invitation);

        return "success";
    }*/

}
