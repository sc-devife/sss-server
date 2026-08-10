package com.sss.app.controller.itinerary;

import com.sss.app.dto.itinerary.ItineraryCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryResponseDTO;
import com.sss.app.dto.itinerary.ItineraryUpdateRequestDTO;
import com.sss.app.service.itinerary.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping
    public ResponseEntity<ItineraryResponseDTO> create(@Valid @RequestBody ItineraryCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryService.create(request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping("/{uid}")
    public ResponseEntity<ItineraryResponseDTO> getByUid(@PathVariable UUID uid) {
        return ResponseEntity.ok(itineraryService.getByUid(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping
    public ResponseEntity<List<ItineraryResponseDTO>> getAllForEscape(@RequestParam UUID escapeUid) {
        return ResponseEntity.ok(itineraryService.getAllForEscape(escapeUid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PutMapping("/{uid}")
    public ResponseEntity<ItineraryResponseDTO> update(@PathVariable UUID uid, @RequestBody ItineraryUpdateRequestDTO request) {
        return ResponseEntity.ok(itineraryService.update(uid, request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uid) {
        itineraryService.delete(uid);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{uid}/new-version")
    public ResponseEntity<ItineraryResponseDTO> createNewVersion(@PathVariable UUID uid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryService.createNewVersion(uid));
    }
}
