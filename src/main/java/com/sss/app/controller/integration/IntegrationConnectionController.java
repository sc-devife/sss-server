package com.sss.app.controller.integration;

import com.sss.app.dto.integration.IntegrationConnectRequestDTO;
import com.sss.app.dto.integration.IntegrationConnectionResponseDTO;
import com.sss.app.service.integration.IntegrationConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Integrations sits under the Organization admin group per Section 4's nav
// structure, so this reuses organizations.read/write rather than minting a
// new permission pair for what's ultimately an org-settings screen.
@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
public class IntegrationConnectionController {

    private final IntegrationConnectionService integrationConnectionService;

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping
    public ResponseEntity<List<IntegrationConnectionResponseDTO>> list() {
        return ResponseEntity.ok(integrationConnectionService.listForOrg());
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping("/{channelCode}/connect")
    public ResponseEntity<IntegrationConnectionResponseDTO> connect(@PathVariable String channelCode,
                                                                      @RequestBody IntegrationConnectRequestDTO request) {
        return ResponseEntity.ok(integrationConnectionService.connect(channelCode, request));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping("/{channelCode}/disconnect")
    public ResponseEntity<IntegrationConnectionResponseDTO> disconnect(@PathVariable String channelCode) {
        return ResponseEntity.ok(integrationConnectionService.disconnect(channelCode));
    }
}
