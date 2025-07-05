package com.sss.app.controller;

import com.sss.app.dto.UserDto;
import com.sss.app.service.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @RequestMapping("/{uid}")
    private ResponseEntity<UserDto> getUserByUid(@PathVariable String uid) {
        return ResponseEntity.ok(usersService.getUserByUid(uid));
    }

}
