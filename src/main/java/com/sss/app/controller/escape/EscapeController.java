package com.sss.app.controller.escape;

import com.sss.app.dto.audit.AuditLogResponseDTO;
import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.dto.escape.TripAdvanceRequestDTO;
import com.sss.app.dto.escape.TripCancelRequestDTO;
import com.sss.app.entity.audit.AuditLog;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.escape.EscapeService;
import com.sss.app.service.escape.TripLifecycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/escape")
@RequiredArgsConstructor
public class EscapeController {

    private final EscapeService escapeService;
    private final TripLifecycleService tripLifecycleService;
    private final AuditLogService auditLogService;

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

    // ----- Lifecycle (Section 8): the only way a trip's status changes. -----

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{id}/advance")
    public ResponseEntity<EscapeResponseDTO> advance(@PathVariable Long id, @Valid @RequestBody TripAdvanceRequestDTO body) {
        return ResponseEntity.ok(tripLifecycleService.advance(id, body.getTargetStatus()));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<EscapeResponseDTO> cancel(@PathVariable Long id, @RequestBody TripCancelRequestDTO body) {
        return ResponseEntity.ok(tripLifecycleService.cancel(id, body.getReason()));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping("/{id}/audit-log")
    public ResponseEntity<List<AuditLogResponseDTO>> auditLog(@PathVariable Long id) {
        escapeService.getTripById(id); // confirms it exists in the caller's own org
        List<AuditLogResponseDTO> history = auditLogService.history("Escape", id).stream()
                .map(this::toAuditLogResponse)
                .toList();
        return ResponseEntity.ok(history);
    }

    private AuditLogResponseDTO toAuditLogResponse(AuditLog log) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        BeanUtils.copyProperties(log, dto);
        return dto;
    }
}
