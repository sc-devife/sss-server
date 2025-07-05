package com.sss.app.controller;

import com.sss.app.dto.OrganizationsDto;
import com.sss.app.dto.UserDto;
import com.sss.app.service.OrganizationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organizations")
public class OrganizationsController {

    private final OrganizationsService organizationsService;

    public OrganizationsController(OrganizationsService organizationsService) {
        this.organizationsService = organizationsService;
    }

    @RequestMapping("/{registeredName}")
    private ResponseEntity<OrganizationsDto> getOrgByRegisteredName(@PathVariable String registeredName) {
        return ResponseEntity.ok(organizationsService.getUserByRegisteredName(registeredName));
    }
}
