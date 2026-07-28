package com.sss.app.controller.traveller;

import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerResponseDTO;
import com.sss.app.dto.traveller.TravellerUpdateRequestDTO;
import com.sss.app.service.traveller.TravellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// No dedicated travellers.* permission exists — travellers only matter in
// the context of trips, so this reuses trips.read/trips.write rather than
// minting a new resource for what's really a sub-concept of Trip.
@RestController
@RequestMapping("/traveller")
@RequiredArgsConstructor
public class TravellerController {
    private final TravellerService travellerService;

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TravellerResponseDTO> createTraveller(@Valid @RequestBody TravellerCreateRequestDTO payload) {
        return ResponseEntity.ok(travellerService.createTraveller(payload));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PutMapping(value = "/{id}/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TravellerResponseDTO> updateTraveller(
            @PathVariable Long id,
            @Valid @RequestBody TravellerUpdateRequestDTO payload) {

        return ResponseEntity.ok(travellerService.updateTraveller(id, payload));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TravellerResponseDTO> getTravellerById(@PathVariable Long id) {
        return ResponseEntity.ok(travellerService.getTravellerById(id));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TravellerResponseDTO>> getAllTravellers() {
        return ResponseEntity.ok(travellerService.getAllTravellers());
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraveller(@PathVariable Long id) {
        travellerService.deleteTraveller(id);
        return ResponseEntity.noContent().build();
    }
}
