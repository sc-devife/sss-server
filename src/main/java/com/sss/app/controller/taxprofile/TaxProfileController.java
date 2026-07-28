package com.sss.app.controller.taxprofile;

import com.sss.app.dto.taxprofile.TaxProfileCreateRequestDTO;
import com.sss.app.dto.taxprofile.TaxProfileResponseDTO;
import com.sss.app.dto.taxprofile.TaxProfileUpdateRequestDTO;
import com.sss.app.service.taxprofile.TaxProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Tax profiles are an Organization settings concept (Section 3 nav grouping),
// so this reuses organizations.read/write rather than a new permission pair.
@RestController
@RequestMapping("/api/tax-profiles")
@RequiredArgsConstructor
public class TaxProfileController {

    private final TaxProfileService taxProfileService;

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping
    public ResponseEntity<TaxProfileResponseDTO> create(@Valid @RequestBody TaxProfileCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taxProfileService.create(request));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping("/{uid}")
    public ResponseEntity<TaxProfileResponseDTO> getByUid(@PathVariable UUID uid) {
        return ResponseEntity.ok(taxProfileService.getByUid(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping
    public ResponseEntity<List<TaxProfileResponseDTO>> getAllForOrg() {
        return ResponseEntity.ok(taxProfileService.getAllForOrg());
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PutMapping("/{uid}")
    public ResponseEntity<TaxProfileResponseDTO> update(@PathVariable UUID uid, @RequestBody TaxProfileUpdateRequestDTO request) {
        return ResponseEntity.ok(taxProfileService.update(uid, request));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping("/{uid}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID uid) {
        taxProfileService.deactivate(uid);
        return ResponseEntity.noContent().build();
    }
}
