package com.sss.app.controller;

import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        return ResponseEntity.ok(usersService.getCurrentUser());
    }

    @RequestMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponseDto>> fetchAllUsers() {
        return ResponseEntity.ok(usersService.fetchAllUsers(123456L));
    }

    @RequestMapping(value = "/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> getUserByUid(@PathVariable String uid) {
        return ResponseEntity.ok(usersService.getUserByUid(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('users.write')")
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto payload) {
        return ResponseEntity.ok(usersService.createUser(payload));
    }

    @PreAuthorize("@permissionService.hasPermission('users.write')")
    @PutMapping(value = "{uid}/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable String uid, @Valid @RequestBody UserUpdateRequestDto payload) {
        return ResponseEntity.ok(usersService.updateUser(uid, payload));
    }

    @PreAuthorize("@permissionService.hasPermission('users.write')")
    @PutMapping(value = "{uid}/assign/roles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> reassignRoles(@PathVariable String uid, @Valid @RequestBody List<String> roles) {
        return ResponseEntity.ok(usersService.reassignRoles(uid, roles));
    }
}
