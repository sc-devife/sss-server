package com.sss.app.controller;

import com.sss.app.dto.organizations.OrganizationSettingsDto;
import com.sss.app.service.OrganizationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Behavioral/config settings for the caller's own organization — split out
// of OrganizationsController so profile identity fields (name, logo, PAN...)
// and settings (currency, timezone, auto-assign...) don't share one PUT
// contract. Backed by the separate organization_settings table.
@RestController
@RequestMapping("/organizations/settings")
public class OrganizationSettingsController {

    private final OrganizationsService organizationsService;

    public OrganizationSettingsController(OrganizationsService organizationsService) {
        this.organizationsService = organizationsService;
    }

    @GetMapping
    public ResponseEntity<OrganizationSettingsDto> getMySettings() {
        return ResponseEntity.ok(organizationsService.getMySettings());
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PutMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<OrganizationSettingsDto> updateMySettings(@RequestBody OrganizationSettingsDto request) {
        return ResponseEntity.ok(organizationsService.updateMySettings(request));
    }
}
