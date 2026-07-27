package com.sss.app.controller.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.service.escape.EscapeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/escape")
@RequiredArgsConstructor
public class EscapeController {

    private final EscapeService escapeService;

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/create")
    public ResponseEntity<EscapeResponseDTO> createTrip(
            @Valid @RequestBody EscapeCreateRequestDTO request) {
        return ResponseEntity.ok(escapeService.createEscape(request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PutMapping("/update/{id}")
    public ResponseEntity<EscapeResponseDTO> updateTrip(
           @PathVariable Long id,
           @Valid @RequestBody EscapeUpdateRequestDTO request) {
        return ResponseEntity.ok(escapeService.updateEscape(id, request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping("/{id}")
    public ResponseEntity<EscapeResponseDTO> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(escapeService.getTripById(id));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping
    public ResponseEntity<List<EscapeResponseDTO>> getAllTrips() {
        return ResponseEntity.ok(escapeService.getAllEscapes());
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTrip(@PathVariable Long id) {
        escapeService.deleteEscape(id);
        return ResponseEntity.ok("Trip deleted successfully with id: " + id);
    }
}
