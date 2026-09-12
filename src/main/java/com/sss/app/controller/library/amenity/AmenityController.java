package com.sss.app.controller.library.amenity;

import com.sss.app.dto.library.amenity.AmenityCreateRequestDTO;
import com.sss.app.dto.library.amenity.AmenityResponseDTO;
import com.sss.app.service.library.amenity.AmenityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Amenities are only ever created/selected from the Add/Edit Hotel form's
// Amenities picker — there is no standalone Amenities module, so this
// controller only needs create + list, unlike Service/RoomType/MealPlan.
@RestController
@RequestMapping("/api/v1/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping
    public ResponseEntity<AmenityResponseDTO> create(@Valid @RequestBody AmenityCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(amenityService.create(dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping
    public ResponseEntity<List<AmenityResponseDTO>> getAll() {
        return ResponseEntity.ok(amenityService.getAll());
    }
}
