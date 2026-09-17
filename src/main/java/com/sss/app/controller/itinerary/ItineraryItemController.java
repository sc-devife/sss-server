package com.sss.app.controller.itinerary;

import com.sss.app.dto.itinerary.ItineraryItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReorderDaysRequestDTO;
import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.itinerary.ItineraryItemReorderRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReplaceRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryItemUpdateRequestDTO;
import com.sss.app.dto.library.transport.TransportCancellationEmailPreviewDTO;
import com.sss.app.dto.library.transport.TransportCancellationEmailSendRequestDTO;
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

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/reorder-days")
    public ResponseEntity<List<ItineraryItemResponseDTO>> reorderDays(@Valid @RequestBody ItineraryItemReorderDaysRequestDTO request) {
        return ResponseEntity.ok(itineraryItemService.reorderDays(request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{uid}/replace")
    public ResponseEntity<ItineraryItemResponseDTO> replaceHotel(@PathVariable UUID uid, @Valid @RequestBody ItineraryItemReplaceRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryItemService.replaceHotel(uid, request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping("/{uid}/cancellation-email-preview")
    public ResponseEntity<TransportCancellationEmailPreviewDTO> getCancellationEmailPreview(@PathVariable UUID uid) {
        return ResponseEntity.ok(itineraryItemService.getTransportCancellationEmailPreview(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{uid}/send-cancellation-email")
    public ResponseEntity<SendEmailResponseDTO> sendCancellationEmail(@PathVariable UUID uid,
                                                                       @RequestBody(required = false) TransportCancellationEmailSendRequestDTO dto) {
        String subject = dto != null ? dto.getSubject() : null;
        return ResponseEntity.ok(itineraryItemService.sendTransportCancellationEmail(uid, subject));
    }
}
