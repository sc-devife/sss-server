package com.sss.app.controller;

import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto dto) {
        System.out.println("Incoming DTO: " + dto);
        System.out.println("Incoming contact_number: " + dto.getContact_number());
        return ResponseEntity.ok(usersService.createUser(dto));
    }
}
