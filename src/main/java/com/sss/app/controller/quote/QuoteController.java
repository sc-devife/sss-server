package com.sss.app.controller.quote;

import com.sss.app.dto.quote.QuoteComputeRequestDTO;
import com.sss.app.dto.quote.QuoteComputeResponseDTO;
import com.sss.app.dto.quote.QuoteCreateRequestDTO;
import com.sss.app.dto.quote.QuoteResponseDTO;
import com.sss.app.dto.quote.QuoteUpdateRequestDTO;
import com.sss.app.service.quote.QuoteComputationService;
import com.sss.app.service.quote.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;
    private final QuoteComputationService quoteComputationService;

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping
    public ResponseEntity<QuoteResponseDTO> create(@Valid @RequestBody QuoteCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteService.create(request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping("/{uid}")
    public ResponseEntity<QuoteResponseDTO> getByUid(@PathVariable UUID uid) {
        return ResponseEntity.ok(quoteService.getByUid(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping
    public ResponseEntity<List<QuoteResponseDTO>> getAllForItinerary(@RequestParam UUID itineraryUid) {
        return ResponseEntity.ok(quoteService.getAllForItinerary(itineraryUid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PutMapping("/{uid}")
    public ResponseEntity<QuoteResponseDTO> update(@PathVariable UUID uid, @RequestBody QuoteUpdateRequestDTO request) {
        return ResponseEntity.ok(quoteService.update(uid, request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uid) {
        quoteService.delete(uid);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{uid}/revise")
    public ResponseEntity<QuoteResponseDTO> createRevision(@PathVariable UUID uid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteService.createRevision(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{uid}/compute")
    public ResponseEntity<QuoteComputeResponseDTO> compute(@PathVariable UUID uid, @RequestBody QuoteComputeRequestDTO request) {
        return ResponseEntity.ok(quoteComputationService.compute(uid, request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{uid}/mark-sent")
    public ResponseEntity<QuoteResponseDTO> markSent(@PathVariable UUID uid) {
        return ResponseEntity.ok(quoteService.markSent(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{uid}/mark-rejected")
    public ResponseEntity<QuoteResponseDTO> markRejected(@PathVariable UUID uid) {
        return ResponseEntity.ok(quoteService.markRejected(uid));
    }
}
