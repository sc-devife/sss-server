package com.sss.app.controller;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class OrganizationAddressController {

    private final AddressService addressService;

    // Create (POST)
    @PostMapping
    public ResponseEntity<AddressDto> createAddress(@RequestBody AddressDto dto) {
        System.out.println("Create Address Started ===");
        return ResponseEntity.ok(addressService.createOrganizationAddress(dto));
    }

    // Update (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable String id,
            @RequestBody AddressDto dto) {
        System.out.println("Calling Update ==" + id );
        return ResponseEntity.ok(addressService.updateOrganizationAddress(id, dto));
    }

    // Get all addresses for an organization
    @GetMapping("/organization/{orgId}")
    public ResponseEntity<List<AddressDto>> getAddressesByOrganization(@PathVariable Long orgId) {
        return ResponseEntity.ok(addressService.getAddressesByOrganization(orgId));
    }
}
