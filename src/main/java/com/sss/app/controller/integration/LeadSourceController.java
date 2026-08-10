package com.sss.app.controller.integration;

import com.sss.app.dto.integration.IntegrationConnectionResponseDTO;
import com.sss.app.dto.integration.meta.LeadImportAttemptResponseDTO;
import com.sss.app.dto.integration.meta.MetaFieldMappingDTO;
import com.sss.app.dto.integration.meta.MetaWebhookEventResponseDTO;
import com.sss.app.service.integration.meta.LeadSourceService;
import com.sss.app.service.integration.meta.MetaResyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Day-to-day operational view of connected lead sources (sync history,
 * webhook/import logs, resync, field mapping). Gated by the existing
 * leads.read/leads.write permissions rather than organizations.* — this is
 * sales-ops visibility, not an org-admin credentials screen (that's still
 * IntegrationConnectionController).
 */
@RestController
@RequestMapping("/api/lead-sources")
@RequiredArgsConstructor
public class LeadSourceController {

    private final LeadSourceService leadSourceService;
    private final MetaResyncService metaResyncService;

    @PreAuthorize("@permissionService.hasPermission('leads.read')")
    @GetMapping
    public ResponseEntity<List<IntegrationConnectionResponseDTO>> list() {
        return ResponseEntity.ok(leadSourceService.listMetaSources());
    }

    @PreAuthorize("@permissionService.hasPermission('leads.read')")
    @GetMapping("/{channelCode}/webhook-events")
    public ResponseEntity<Page<MetaWebhookEventResponseDTO>> webhookEvents(@PathVariable String channelCode, Pageable pageable) {
        return ResponseEntity.ok(leadSourceService.listWebhookEvents(channelCode, pageable));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.read')")
    @GetMapping("/{channelCode}/import-attempts")
    public ResponseEntity<Page<LeadImportAttemptResponseDTO>> importAttempts(@PathVariable String channelCode, Pageable pageable) {
        return ResponseEntity.ok(leadSourceService.listImportAttempts(channelCode, pageable));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PostMapping("/import-attempts/{id}/resync")
    public ResponseEntity<LeadImportAttemptResponseDTO> resync(@PathVariable UUID id) {
        return ResponseEntity.ok(leadSourceService.toResponseDto(metaResyncService.resync(id)));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.read')")
    @GetMapping("/{channelCode}/field-mappings")
    public ResponseEntity<List<MetaFieldMappingDTO>> fieldMappings(@PathVariable String channelCode) {
        return ResponseEntity.ok(leadSourceService.listFieldMappings(channelCode));
    }

    @PreAuthorize("@permissionService.hasPermission('leads.write')")
    @PutMapping("/{channelCode}/field-mappings")
    public ResponseEntity<MetaFieldMappingDTO> saveFieldMapping(@PathVariable String channelCode, @RequestBody MetaFieldMappingDTO request) {
        return ResponseEntity.ok(leadSourceService.saveFieldMapping(channelCode, request));
    }
}
