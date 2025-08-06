package com.sss.app.controller;

import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.service.OrganizationsService;
import jakarta.validation.Valid;
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
        return ResponseEntity.ok(organizationsService.getOrganizationsRegisteredName(registeredName));
    }

    @PostMapping("/create")
    private ResponseEntity<OrganizationsDto> createOrganizations(@RequestBody OrganizationsDto request) {
        return ResponseEntity.ok(organizationsService.createOrganizations(request));
    }

   @PutMapping(value = "{uid}/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<OrganizationsDto> updateOrganizations(@PathVariable String uid, @Valid @RequestBody OrganizationsDto request) {
        return ResponseEntity.ok(organizationsService.updateOrganizations(uid, request));
    }

    @DeleteMapping("/{registeredName}")
    public ResponseEntity<String> deleteOrganization(@PathVariable String registeredName) {
        organizationsService.deleteOrganizations(registeredName);
        return ResponseEntity.ok("Organization deleted successfully");
    }
}
