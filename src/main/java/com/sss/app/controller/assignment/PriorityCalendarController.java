package com.sss.app.controller.assignment;

import com.sss.app.dto.assignment.PriorityCalendarEntryCreateRequestDTO;
import com.sss.app.dto.assignment.PriorityCalendarEntryResponseDTO;
import com.sss.app.service.assignment.PriorityCalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Assignment Rules is an org-settings screen (Section 4 nav grouping), so
// this reuses organizations.read/write rather than a new permission pair —
// same convention as ReminderRuleController/TaxProfileController.
@RestController
@RequestMapping("/api/priority-calendar")
@RequiredArgsConstructor
public class PriorityCalendarController {

    private final PriorityCalendarService priorityCalendarService;

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping
    public ResponseEntity<List<PriorityCalendarEntryResponseDTO>> getAllForOrg() {
        return ResponseEntity.ok(priorityCalendarService.getAllForOrg());
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping
    public ResponseEntity<PriorityCalendarEntryResponseDTO> create(@Valid @RequestBody PriorityCalendarEntryCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(priorityCalendarService.create(request));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uid) {
        priorityCalendarService.delete(uid);
        return ResponseEntity.noContent().build();
    }
}
