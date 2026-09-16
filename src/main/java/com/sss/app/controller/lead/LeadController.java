package com.sss.app.controller.lead;

import com.sss.app.dto.audit.AuditLogResponseDTO;
import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.lead.LeadAssignRequestDTO;
import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadFollowUpDueDateRequestDTO;
import com.sss.app.dto.lead.LeadReasonActionRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.lead.LeadLifecycleService;
import com.sss.app.service.lead.LeadService;
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

import java.time.LocalDate;
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

    // Field-level edit — distinct from the lifecycle actions below (Section
    // 7), which are the only way status itself changes.
    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> updateLead(@PathVariable UUID id, @Valid @RequestBody LeadCreateRequestDTO payload) {
        return ResponseEntity.ok(leadService.updateLead(id, payload));
    }

    // Leads page's Search + Status + Priority + Month/Week/Day/All +
    // pagination — all handled at the DB level (LeadsHelper.getAllLeads),
    // never by fetching everything and filtering/paginating in the browser.
    // `page`/`size`/`sort` are Spring Data Web's standard Pageable query
    // params (already this app's convention — see LeadSourceController's
    // webhook-events/import-attempts endpoints); defaults to newest-first,
    // 20/page, so a caller that omits them entirely still gets sensible
    // behavior. `priority=true` is the Status dropdown's separate "Priority"
    // pseudo-status (filters Lead.isPriority, not Lead.status — mutually
    // exclusive with `status` in the frontend, but nothing stops both being
    // sent, which the query would just AND together). `from`/`to` (both
    // "YYYY-MM-DD", inclusive) back the date-period filter — omit both for
    // "All" (no date restriction). `to` is treated as a whole day (converted
    // to the exclusive instant of the *next* day) so the last day of the
    // range is always fully included.
    // `escapePointId` (Section 15+ toolbar filter, between Search and Status)
    // matches EscapePoint.uid, not Lead.uid. `source` (More Filters' Source
    // multi-select) may repeat — "agency" plus any of the DIRECT
    // sourceChannel values (manual/whatsapp/instagram/youtube/google_ads).
    // `archive` (More Filters' Archive checkbox) defaults false — non-
    // archived leads only — and is an exclusive toggle when true (only
    // archived leads), not an additive "include archived too".
    @PreAuthorize("@permissionService.hasPermission('leads.read')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<LeadResponseDTO>> getAllLeads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean priority,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String escapePointId,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false, defaultValue = "false") Boolean archive,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var start = from != null ? from.atStartOfDay() : null;
        var end = to != null ? to.plusDays(1).atStartOfDay() : null;
        return ResponseEntity.ok(leadService.getAllLeads(search, status, priority, start, end, escapePointId, source, archive, pageable));
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

    // Soft-delete — removes the lead from the default Leads list without
    // changing its status. Mirrors the Hotel/EscapePoint DELETE-as-archive
    // convention, but kept as a lifecycle action (not a DELETE mapping)
    // since Lead has no hard-delete path at all.
    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping(value = "/{id}/actions/archive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        leadLifecycleService.archive(id);
        return ResponseEntity.noContent().build();
    }

    // Not a lifecycle action — a plain field, so a direct setter rather than
    // going through LeadLifecycleService's status-transition machinery.
    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PutMapping(value = "/{id}/follow-up-due-date", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> setFollowUpDueDate(@PathVariable UUID id, @RequestBody LeadFollowUpDueDateRequestDTO body) {
        return ResponseEntity.ok(leadService.setFollowUpDueDate(id, body.getFollowUpDueDate()));
    }

    // Manual (re)assignment — the auto-assignment engine already runs once
    // at intake (see LeadsHelper.createLead); this covers reassigning
    // afterward or picking up a lead that was left unassigned.
    @PreAuthorize("@permissionService.hasPermission('leads.assign')")
    @PostMapping(value = "/{id}/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LeadResponseDTO> assign(@PathVariable UUID id, @Valid @RequestBody LeadAssignRequestDTO body) {
        return ResponseEntity.ok(leadService.assignLead(id, body.getUserId(), body.getReason()));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.read')")
    @GetMapping(value = "/{id}/audit-log", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuditLogResponseDTO>> auditLog(@PathVariable UUID id) {
        // Confirms the lead exists in the caller's own org before exposing its history.
        Long seqp = leadService.resolveSeqp(id);
        return ResponseEntity.ok(auditLogService.historyResponses("Lead", seqp));
    }
}
