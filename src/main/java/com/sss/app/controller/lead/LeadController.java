package com.sss.app.controller.lead;

import com.sss.app.dto.audit.AuditLogResponseDTO;
import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadFollowUpDueDateRequestDTO;
import com.sss.app.dto.lead.LeadReasonActionRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.lead.LeadLifecycleService;
import com.sss.app.service.lead.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {
    private final LeadService leadService;
    private final LeadLifecycleService leadLifecycleService;
    private final AuditLogService auditLogService;

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> createLead(@Valid @RequestBody LeadCreateRequestDTO payload) {
        return ResponseEntity.ok(leadService.createLead(payload));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.read')")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> getLeadById(@PathVariable UUID id) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.read')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LeadResponseDTO>> getAllLeads() {
        return ResponseEntity.ok(leadService.getAllLeads());
    }

    // ----- Lifecycle actions (Section 7): the only way a lead's status
    // changes — no generic PATCH/PUT on status exists, by design. -----

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping(value = "/{id}/actions/contact", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> contact(@PathVariable UUID id) {
        return ResponseEntity.ok(leadLifecycleService.contact(id));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping(value = "/{id}/actions/qualify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> qualify(@PathVariable UUID id) {
        return ResponseEntity.ok(leadLifecycleService.qualify(id));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping(value = "/{id}/actions/disqualify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> disqualify(@PathVariable UUID id, @RequestBody LeadReasonActionRequestDTO body) {
        return ResponseEntity.ok(leadLifecycleService.disqualify(id, body.getReason()));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping(value = "/{id}/actions/mark-lost", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> markLost(@PathVariable UUID id, @RequestBody LeadReasonActionRequestDTO body) {
        return ResponseEntity.ok(leadLifecycleService.markLost(id, body.getReason()));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping(value = "/{id}/actions/mark-duplicate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> markDuplicate(@PathVariable UUID id, @RequestBody LeadReasonActionRequestDTO body) {
        return ResponseEntity.ok(leadLifecycleService.markDuplicate(id, body.getReason()));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping(value = "/{id}/actions/convert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapeResponseDTO> convert(@PathVariable UUID id, @Valid @RequestBody EscapeCreateRequestDTO request) {
        return ResponseEntity.ok(leadLifecycleService.convertToEscape(id, request));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping(value = "/{id}/actions/toggle-priority", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> togglePriority(@PathVariable UUID id) {
        return ResponseEntity.ok(leadLifecycleService.togglePriority(id));
    }

    // Not a lifecycle action — a plain field, so a direct setter rather than
    // going through LeadLifecycleService's status-transition machinery.
    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PutMapping(value = "/{id}/follow-up-due-date", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> setFollowUpDueDate(@PathVariable UUID id, @RequestBody LeadFollowUpDueDateRequestDTO body) {
        return ResponseEntity.ok(leadService.setFollowUpDueDate(id, body.getFollowUpDueDate()));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.read')")
    @GetMapping(value = "/{id}/audit-log", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuditLogResponseDTO>> auditLog(@PathVariable UUID id) {
        // Confirms the lead exists in the caller's own org before exposing its history.
        Long seqp = leadService.resolveSeqp(id);
        return ResponseEntity.ok(auditLogService.historyResponses("Lead", seqp));
    }
}
