package com.sss.app.controller.itinerary;

import com.sss.app.dto.itinerary.ItineraryContentItemAttachRequestDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemUpdateRequestDTO;
import com.sss.app.service.itinerary.ItineraryContentItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/itinerary-content-items")
@RequiredArgsConstructor
public class ItineraryContentItemController {

    private final ItineraryContentItemService itineraryContentItemService;

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/attach")
    public ResponseEntity<ItineraryContentItemResponseDTO> attach(@Valid @RequestBody ItineraryContentItemAttachRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryContentItemService.attach(request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping
    public ResponseEntity<ItineraryContentItemResponseDTO> create(@Valid @RequestBody ItineraryContentItemCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryContentItemService.create(request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping
    public ResponseEntity<List<ItineraryContentItemResponseDTO>> getAllForItinerary(@RequestParam UUID itineraryUid) {
        return ResponseEntity.ok(itineraryContentItemService.getAllForItinerary(itineraryUid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PutMapping("/{uid}")
    public ResponseEntity<ItineraryContentItemResponseDTO> update(@PathVariable UUID uid, @RequestBody ItineraryContentItemUpdateRequestDTO request) {
        return ResponseEntity.ok(itineraryContentItemService.update(uid, request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uid) {
        itineraryContentItemService.delete(uid);
        return ResponseEntity.noContent().build();
    }
}
