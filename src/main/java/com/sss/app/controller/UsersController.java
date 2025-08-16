package com.sss.app.controller;

import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @RequestMapping("/{uid}")
    private ResponseEntity<UserResponseDto> getUserByUid(@PathVariable String uid) {
        return ResponseEntity.ok(usersService.getUserByUid(uid));
    }

    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto payload) {
        return ResponseEntity.ok(usersService.createUser(payload));
    }

    @PutMapping(value = "{uid}/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable String uid, @Valid @RequestBody UserUpdateRequestDto payload) {
        System.out.println("Incoming payload: " + payload);
        System.out.println("Incoming contact_number: " + payload.getContact_number());
        return ResponseEntity.ok(usersService.updateUser(uid, payload));
    }

    @PutMapping(value = "{uid}/assign/roles", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponseDto> reassignRoles(@PathVariable String uid, @Valid @RequestBody List<String> roles) {
        System.out.println("Incoming roles: " + roles);
        return ResponseEntity.ok(usersService.reassignRoles(uid, roles));
    }
}
