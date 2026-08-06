package com.sss.app.controller;

import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.service.OrganizationsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
public class OrganizationsController {

    private final OrganizationsService organizationsService;

    public OrganizationsController(OrganizationsService organizationsService) {
        this.organizationsService = organizationsService;
    }

    @GetMapping("/mine")
    public ResponseEntity<OrganizationsDto> getMyOrganization() {
        return ResponseEntity.ok(organizationsService.getMyOrganization());
    }

    @GetMapping("/{uid}")
    public ResponseEntity<OrganizationsDto> getOrgByUid(@PathVariable String uid) {
        return ResponseEntity.ok(organizationsService.getOrganizationsByUid(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping("/create")
    public ResponseEntity<OrganizationsDto> createOrganizations(@Valid @RequestBody OrganizationsDto request) {
        return ResponseEntity.ok(organizationsService.createOrganizations(request));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PutMapping(value = "{uid}/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<OrganizationsDto> updateOrganizations(@PathVariable String uid, @Valid @RequestBody OrganizationsDto request) {
        return ResponseEntity.ok(organizationsService.updateOrganizations(uid, request));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @DeleteMapping("/{registeredName}")
    public ResponseEntity<String> deleteOrganization(@PathVariable String registeredName) {
        organizationsService.deleteOrganizations(registeredName);
        return ResponseEntity.ok("Organization deleted successfully");
    }
}
