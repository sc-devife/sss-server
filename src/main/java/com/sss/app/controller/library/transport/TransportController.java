package com.sss.app.controller.library.transport;

import com.sss.app.dto.library.transport.TransportCreateRequestDTO;
import com.sss.app.dto.library.transport.TransportResponseDTO;
import com.sss.app.dto.library.transport.TransportUpdateRequestDTO;
import com.sss.app.service.library.transport.TransportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transports")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping
    public ResponseEntity<TransportResponseDTO> create(@Valid @RequestBody TransportCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transportService.create(dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}")
    public ResponseEntity<TransportResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(transportService.getById(id));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping
    public ResponseEntity<List<TransportResponseDTO>> getAll() {
        return ResponseEntity.ok(transportService.getAll());
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping("/{id}")
    public ResponseEntity<TransportResponseDTO> update(@PathVariable UUID id,
                                                         @RequestBody TransportUpdateRequestDTO dto) {
        return ResponseEntity.ok(transportService.update(id, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
