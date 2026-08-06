package com.sss.app.controller;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Contact addresses are an org-settings concept (Section 4 nav grouping), so
// this reuses organizations.read/write rather than a new permission pair —
// same convention as TaxProfile/ReminderRule.
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class OrganizationAddressController {

    private final AddressService addressService;

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping(value = "/{orgId}/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AddressDto> createAddress(@PathVariable Long orgId, @Valid @RequestBody AddressDto dto) {
        return ResponseEntity.ok(addressService.createOrganizationAddress(orgId, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PutMapping(value = "/{orgId}/update/{addressId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Long orgId,
                                                    @PathVariable Long addressId,
                                                    @Valid @RequestBody AddressDto dto) {
        return ResponseEntity.ok(addressService.updateOrganizationAddress(orgId, addressId, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping("/{orgId}")
    public ResponseEntity<List<AddressDto>> getAddressesForOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(addressService.getAddressesForOrg(orgId));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @DeleteMapping("/{orgId}/address/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long orgId,
                                              @PathVariable Long addressId) {
        addressService.deleteOrganizationAddress(orgId, addressId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }
}
