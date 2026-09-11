package com.sss.app.controller.followup;

import com.sss.app.dto.followup.FollowUpCreateRequestDTO;
import com.sss.app.dto.followup.FollowUpResponseDTO;
import com.sss.app.dto.followup.FollowUpStatusUpdateRequestDTO;
import com.sss.app.dto.followup.FollowUpUpdateRequestDTO;
import com.sss.app.service.followup.FollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {
    private final FollowUpService followUpService;

    @PreAuthorize("@permissionService.hasPermission('followups.write')")
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FollowUpResponseDTO> create(@Valid @RequestBody FollowUpCreateRequestDTO payload) {
        return ResponseEntity.ok(followUpService.createFollowUp(payload));
    }

    // The dedicated /follow-ups page's own list — always scoped to the
    // caller (LeadController's Search+Status+pagination shape).
    // `filter` is one of today|yesterday|overdue|upcoming|all, defaulting to
    // "today" server-side (FollowUpsHelper.getAllForCurrentUser).
    @PreAuthorize("@permissionService.hasPermission('followups.read')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<FollowUpResponseDTO>> getAll(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "dueAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(followUpService.getAllForCurrentUser(filter, search, pageable));
    }

    // Header badge — count of my open actionable follow-ups.
    @PreAuthorize("@permissionService.hasPermission('followups.read')")
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("count", followUpService.countOpenForCurrentUser()));
    }

    // Lead/Escape detail page's "Tasks & Comments" section — every follow-up
    // on that record, any assignee.
    @PreAuthorize("@permissionService.hasPermission('followups.read')")
    @GetMapping(value = "/by-lead/{leadUid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FollowUpResponseDTO>> getAllForLead(@PathVariable UUID leadUid) {
        return ResponseEntity.ok(followUpService.getAllForLead(leadUid));
    }

    @PreAuthorize("@permissionService.hasPermission('followups.read')")
    @GetMapping(value = "/by-escape/{escapeUid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FollowUpResponseDTO>> getAllForEscape(@PathVariable UUID escapeUid) {
        return ResponseEntity.ok(followUpService.getAllForEscape(escapeUid));
    }

    @PreAuthorize("@permissionService.hasPermission('followups.write')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FollowUpResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody FollowUpUpdateRequestDTO payload) {
        return ResponseEntity.ok(followUpService.updateFollowUp(id, payload));
    }

    // The table's inline Pending/Hold/Completed dropdown.
    @PreAuthorize("@permissionService.hasPermission('followups.write')")
    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FollowUpResponseDTO> updateStatus(@PathVariable UUID id, @Valid @RequestBody FollowUpStatusUpdateRequestDTO payload) {
        return ResponseEntity.ok(followUpService.updateStatus(id, payload.getStatus()));
    }
}
