package com.sss.app.controller.quotationtemplate;

import com.sss.app.dto.quotationtemplate.QuotationTemplateResponseDTO;
import com.sss.app.dto.quotationtemplate.QuotationTemplateUpdateRequestDTO;
import com.sss.app.service.quotationtemplate.QuotationTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Quotation template metadata CRUD + the Settings-only sample preview.
 * Actual Escape/Quote preview lives on EscapeController (real data, same
 * QuotationRenderingService) — kept there rather than here since it needs
 * escape-scoped access checks, not template-scoped ones.
 */
@RestController
@RequestMapping("/api/quotation-templates")
@RequiredArgsConstructor
public class QuotationTemplateController {

    private final QuotationTemplateService quotationTemplateService;

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuotationTemplateResponseDTO> create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "previewImage", required = false) MultipartFile previewImage) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quotationTemplateService.create(name, description, file, previewImage));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping
    public ResponseEntity<List<QuotationTemplateResponseDTO>> getAllActive() {
        return ResponseEntity.ok(quotationTemplateService.getAllActive());
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping("/{uid}")
    public ResponseEntity<QuotationTemplateResponseDTO> getById(@PathVariable UUID uid) {
        return ResponseEntity.ok(quotationTemplateService.getById(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PutMapping(value = "/{uid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuotationTemplateResponseDTO> update(
            @PathVariable UUID uid,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "previewImage", required = false) MultipartFile previewImage) {
        QuotationTemplateUpdateRequestDTO dto = new QuotationTemplateUpdateRequestDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setIsActive(isActive);
        return ResponseEntity.ok(quotationTemplateService.update(uid, dto, file, previewImage));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uid) {
        quotationTemplateService.delete(uid);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping("/{uid}/set-default")
    public ResponseEntity<Void> setAsDefault(@PathVariable UUID uid) {
        quotationTemplateService.setAsDefault(uid);
        return ResponseEntity.noContent().build();
    }

    /** Settings-only preview: selected template + predefined sample data — no Escape required. */
    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping(value = "/{uid}/preview-sample", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewWithSampleData(@PathVariable UUID uid) {
        return ResponseEntity.ok(quotationTemplateService.previewWithSampleData(uid));
    }
}
