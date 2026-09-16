package com.sss.app.controller.library.hotel;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.library.hotel.HotelBookingDTO;
import com.sss.app.dto.library.hotel.HotelBookingEmailPreviewDTO;
import com.sss.app.dto.library.hotel.HotelBookingEmailSendRequestDTO;
import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelPaymentCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelPaymentResponseDTO;
import com.sss.app.dto.library.hotel.HotelPriorityImageRequestDTO;
import com.sss.app.dto.library.hotel.HotelResponseDTO;
import com.sss.app.dto.library.hotel.HotelUpdateRequestDTO;
import com.sss.app.service.library.hotel.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping
    public ResponseEntity<HotelResponseDTO> create(@Valid @RequestBody HotelCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.create(dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(hotelService.getById(id));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping
    public ResponseEntity<List<HotelResponseDTO>> getAll() {
        return ResponseEntity.ok(hotelService.getAll());
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> update(@PathVariable UUID id,
                                                     @Valid @RequestBody HotelUpdateRequestDTO dto) {
        return ResponseEntity.ok(hotelService.update(id, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping("/{id}/priority-image")
    public ResponseEntity<HotelResponseDTO> setPriorityImage(@PathVariable UUID id,
                                                               @Valid @RequestBody HotelPriorityImageRequestDTO dto) {
        return ResponseEntity.ok(hotelService.setPriorityImage(id, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<HotelBookingDTO>> getBookings(@PathVariable UUID id) {
        return ResponseEntity.ok(hotelService.getBookings(id));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping("/{id}/bookings/{itineraryItemUid}/mark-booked")
    public ResponseEntity<HotelBookingDTO> markBooked(@PathVariable UUID id, @PathVariable UUID itineraryItemUid) {
        return ResponseEntity.ok(hotelService.markBooked(id, itineraryItemUid));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}/bookings/{itineraryItemUid}/booking-email-preview")
    public ResponseEntity<HotelBookingEmailPreviewDTO> getBookingEmailPreview(@PathVariable UUID id, @PathVariable UUID itineraryItemUid) {
        return ResponseEntity.ok(hotelService.getBookingEmailPreview(id, itineraryItemUid));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping("/{id}/bookings/{itineraryItemUid}/send-email")
    public ResponseEntity<SendEmailResponseDTO> sendBookingEmail(@PathVariable UUID id, @PathVariable UUID itineraryItemUid,
                                                                    @RequestBody(required = false) HotelBookingEmailSendRequestDTO dto) {
        String subject = dto != null ? dto.getSubject() : null;
        return ResponseEntity.ok(hotelService.sendBookingEmail(id, itineraryItemUid, subject));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}/payments")
    public ResponseEntity<List<HotelPaymentResponseDTO>> getPayments(@PathVariable UUID id) {
        return ResponseEntity.ok(hotelService.getPayments(id));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping("/{id}/payments")
    public ResponseEntity<HotelPaymentResponseDTO> createPayment(@PathVariable UUID id,
                                                                    @Valid @RequestBody HotelPaymentCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.createPayment(id, dto));
    }
}
