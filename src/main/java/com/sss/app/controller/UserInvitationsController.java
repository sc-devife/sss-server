package com.sss.app.controller;

import com.sss.app.dto.users.invitations.InviteUserRequestDto;
import com.sss.app.dto.users.invitations.UserInvitationDto;
import com.sss.app.service.UserInvitationsService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/newuser")
public class UserInvitationsController {

    private final UserInvitationsService userInvitationsService;

    public UserInvitationsController(UserInvitationsService userInvitationsService) {
        this.userInvitationsService = userInvitationsService;
    }

    @PreAuthorize("@permissionService.hasPermission('users.write')")
    @PostMapping(value = "/invite", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInvitationDto> inviteUser(@Valid @RequestBody InviteUserRequestDto request) {
        return ResponseEntity.ok(userInvitationsService.inviteUser(request.getEmail()));
    }
}
