package com.sss.app.controller.deal;

import com.sss.app.dto.deal.DealResponseDTO;
import com.sss.app.service.deal.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/accept-quote/{quoteUid}")
    public ResponseEntity<DealResponseDTO> acceptQuote(@PathVariable UUID quoteUid) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.acceptQuote(quoteUid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping("/{uid}")
    public ResponseEntity<DealResponseDTO> getByUid(@PathVariable UUID uid) {
        return ResponseEntity.ok(dealService.getByUid(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping
    public ResponseEntity<DealResponseDTO> getForTrip(@RequestParam Long tripId) {
        return ResponseEntity.ok(dealService.getForTrip(tripId));
    }
}
