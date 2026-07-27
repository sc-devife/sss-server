package com.sss.app.controller.library.serviceprovider;

import com.sss.app.dto.library.serviceprovider.ServiceProviderCreateRequestDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderResponseDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderUpdateRequestDTO;
import com.sss.app.service.library.serviceprovider.ServiceProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service-providers")
@RequiredArgsConstructor
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping
    public ResponseEntity<ServiceProviderResponseDTO> create(@Valid @RequestBody ServiceProviderCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceProviderService.create(dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceProviderResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceProviderService.getById(id));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping
    public ResponseEntity<List<ServiceProviderResponseDTO>> getAll() {
        return ResponseEntity.ok(serviceProviderService.getAll());
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceProviderResponseDTO> update(@PathVariable UUID id,
                                                               @RequestBody ServiceProviderUpdateRequestDTO dto) {
        return ResponseEntity.ok(serviceProviderService.update(id, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceProviderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
