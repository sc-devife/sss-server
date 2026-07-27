package com.sss.app.controller.library.location;

import com.sss.app.dto.library.location.LocationCreateRequestDTO;
import com.sss.app.dto.library.location.LocationResponseDTO;
import com.sss.app.dto.library.location.LocationUpdateRequestDTO;
import com.sss.app.service.library.location.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping
    public ResponseEntity<LocationResponseDTO> create(@Valid @RequestBody LocationCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.create(dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(locationService.getById(id));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping
    public ResponseEntity<List<LocationResponseDTO>> getAll() {
        return ResponseEntity.ok(locationService.getAll());
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDTO> update(@PathVariable UUID id,
                                                        @RequestBody LocationUpdateRequestDTO dto) {
        return ResponseEntity.ok(locationService.update(id, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
