package com.sss.app.controller;

import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.service.OrganizationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    private ResponseEntity<OrganizationsDto> createOrganizations(@RequestBody OrganizationsDto request) {
        return ResponseEntity.ok(organizationsService.createOrganizations(request));
    }

    @PutMapping("/update")
    private ResponseEntity<OrganizationsDto> updateOrganizations(@RequestBody OrganizationsDto request) {
        return ResponseEntity.ok(organizationsService.updateOrganizations(request));
    }
}
