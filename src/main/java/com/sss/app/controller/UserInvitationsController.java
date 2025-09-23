package com.sss.app.controller;

import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.dto.users.invitations.UserInvitationDto;
import com.sss.app.entity.users.invitations.UserInvitation;
import com.sss.app.repository.InvitationTokenRepository;
import com.sss.app.service.UserInvitationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/newuser")
public class UserInvitationsController {

    // @Autowired
    private UserInvitationsService userInvitationsService;

    public UserInvitationsController(UserInvitationsService userInvitationsService) {
        this.userInvitationsService = userInvitationsService;
    }

    @RequestMapping("/invite")
    public ResponseEntity<UserInvitationDto> inviteUser(@RequestParam String email) {
        return ResponseEntity.ok(userInvitationsService.inviteUser(email));
    }

    private InvitationTokenRepository tokenRepository;
    @RequestMapping("/{uid}")
    public ResponseEntity<UserInvitationDto> getInvitationDetails(@PathVariable String uid)
    {
        System.out.println("uuid::::::::::::" + uid);//uid
        Optional<UserInvitation> optional = tokenRepository.findByUid(uid);
        if (optional.isEmpty()) {
            System.out.println("No invitation found for uid: " + uid);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userInvitationsService.getInvitationDetails(uid));

        //return "URL redirect to registration form with token: " + uid;
    }
}
