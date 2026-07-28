package com.sss.app.controller.payment;

import com.sss.app.dto.payment.PaymentMilestoneCreateRequestDTO;
import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;
import com.sss.app.dto.payment.PaymentRecordRequestDTO;
import com.sss.app.service.payment.PaymentMilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-milestones")
@RequiredArgsConstructor
public class PaymentMilestoneController {

    private final PaymentMilestoneService paymentMilestoneService;

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping
    public ResponseEntity<PaymentMilestoneResponseDTO> create(@Valid @RequestBody PaymentMilestoneCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMilestoneService.create(request));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping
    public ResponseEntity<List<PaymentMilestoneResponseDTO>> getAllForDeal(@RequestParam UUID dealUid) {
        return ResponseEntity.ok(paymentMilestoneService.getAllForDeal(dealUid));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @PostMapping("/{uid}/record-payment")
    public ResponseEntity<PaymentMilestoneResponseDTO> recordPayment(@PathVariable UUID uid, @Valid @RequestBody PaymentRecordRequestDTO request) {
        return ResponseEntity.ok(paymentMilestoneService.recordPayment(uid, request.getAmount()));
    }

    @PreAuthorize("@permissionService.hasPermission('trips.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uid) {
        paymentMilestoneService.delete(uid);
        return ResponseEntity.noContent().build();
    }
}
