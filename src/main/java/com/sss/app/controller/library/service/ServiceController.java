package com.sss.app.controller.library.service;

import com.sss.app.dto.library.service.ServiceCreateRequestDTO;
import com.sss.app.dto.library.service.ServiceResponseDTO;
import com.sss.app.dto.library.service.ServiceUpdateRequestDTO;
import com.sss.app.service.library.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping
    public ResponseEntity<ServiceResponseDTO> create(@Valid @RequestBody ServiceCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceService.create(dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.getById(id));
    }

    // No hotelId -> global master-data services (main Services module).
    // hotelId set -> global services plus that hotel's own scoped ones, for
    // the Hotel Add/Edit form's Services picker.
    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> getAll(@RequestParam(required = false) UUID hotelId) {
        return ResponseEntity.ok(serviceService.getAll(hotelId));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> update(@PathVariable UUID id,
                                                       @RequestBody ServiceUpdateRequestDTO dto) {
        return ResponseEntity.ok(serviceService.update(id, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
