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

    @PostMapping(value = "/{orgId}/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AddressDto> createAddress(@PathVariable Long orgId, @RequestBody AddressDto dto) {
        System.out.println("Create Address Started ===");
        return ResponseEntity.ok(addressService.createOrganizationAddress(orgId, dto));
    }

    @PutMapping(value = "/{orgId}/update/{addressId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Long orgId,
                                                    @PathVariable Long addressId,
                                                    @RequestBody AddressDto dto) {
        System.out.println("Update Address Started ===");
        return ResponseEntity.ok(addressService.updateOrganizationAddress(orgId, addressId, dto));
    }

    @RequestMapping("/{uid}")
    public ResponseEntity<List<AddressDto>> getAddressesByOrg(@PathVariable String uid) {
        List<AddressDto> addresses = addressService.getAddressesByOrganization(uid);
        return ResponseEntity.ok(addresses);
    }

  /*  // Update (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable String id,
            @RequestBody AddressDto dto) {
        System.out.println("Calling Update ==" + id );
        return ResponseEntity.ok(addressService.updateOrganizationAddress(id, dto));
    }*/

    // Get all addresses for an organization
    @GetMapping("/organization/{orgId}")
    public ResponseEntity<List<AddressDto>> getAddressesByOrganization(@PathVariable String orgId) {
        return ResponseEntity.ok(addressService.getAddressesByOrganization(orgId));
    }
}
