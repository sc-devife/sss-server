package com.sss.app.controller.library.activity;

import com.sss.app.dto.library.activity.ActivityBookingDTO;
import com.sss.app.dto.library.activity.ActivityCreateRequestDTO;
import com.sss.app.dto.library.activity.ActivityResponseDTO;
import com.sss.app.dto.library.activity.ActivityUpdateRequestDTO;
import com.sss.app.service.library.activity.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping
    public ResponseEntity<ActivityResponseDTO> create(@Valid @RequestBody ActivityCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.create(dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(activityService.getById(id));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping
    public ResponseEntity<List<ActivityResponseDTO>> getAll() {
        return ResponseEntity.ok(activityService.getAll());
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponseDTO> update(@PathVariable UUID id,
                                                        @RequestBody ActivityUpdateRequestDTO dto) {
        return ResponseEntity.ok(activityService.update(id, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<ActivityBookingDTO>> getBookings(@PathVariable UUID id) {
        return ResponseEntity.ok(activityService.getBookings(id));
    }
}
