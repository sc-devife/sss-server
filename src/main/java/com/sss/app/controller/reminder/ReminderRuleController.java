package com.sss.app.controller.reminder;

import com.sss.app.dto.reminder.ReminderRuleCreateRequestDTO;
import com.sss.app.dto.reminder.ReminderRuleResponseDTO;
import com.sss.app.service.reminder.PaymentReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Reminder cadence is an org-settings concept (Section 8), so this reuses
// organizations.read/write rather than a new permission pair.
@RestController
@RequestMapping("/api/reminder-rules")
@RequiredArgsConstructor
public class ReminderRuleController {

    private final PaymentReminderService paymentReminderService;

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping
    public ResponseEntity<List<ReminderRuleResponseDTO>> getRulesForOrg() {
        return ResponseEntity.ok(paymentReminderService.getRulesForOrg());
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping
    public ResponseEntity<ReminderRuleResponseDTO> createRule(@Valid @RequestBody ReminderRuleCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentReminderService.createRule(request));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID uid) {
        paymentReminderService.deleteRule(uid);
        return ResponseEntity.noContent().build();
    }
}
