package com.sss.app.controller.itinerary;

import com.sss.app.dto.itinerary.ItineraryItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReorderRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryItemUpdateRequestDTO;
import com.sss.app.service.itinerary.ItineraryItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/itinerary-items")
@RequiredArgsConstructor
public class ItineraryItemController {

    private final ItineraryItemService itineraryItemService;

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping
    public ResponseEntity<ItineraryItemResponseDTO> create(@Valid @RequestBody ItineraryItemCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryItemService.create(request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping
    public ResponseEntity<List<ItineraryItemResponseDTO>> getAllForItinerary(@RequestParam UUID itineraryUid) {
        return ResponseEntity.ok(itineraryItemService.getAllForItinerary(itineraryUid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PutMapping("/{uid}")
    public ResponseEntity<ItineraryItemResponseDTO> update(@PathVariable UUID uid, @RequestBody ItineraryItemUpdateRequestDTO request) {
        return ResponseEntity.ok(itineraryItemService.update(uid, request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uid) {
        itineraryItemService.delete(uid);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/reorder")
    public ResponseEntity<List<ItineraryItemResponseDTO>> reorder(@Valid @RequestBody ItineraryItemReorderRequestDTO request) {
        return ResponseEntity.ok(itineraryItemService.reorder(request));
    }
}
