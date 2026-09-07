package com.sss.app.controller.billingtemplate;

import com.sss.app.dto.billingtemplate.BillingTemplateResponseDTO;
import com.sss.app.dto.billingtemplate.BillingTemplateUpdateRequestDTO;
import com.sss.app.service.billingtemplate.BillingTemplateService;
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
 * Invoice template metadata CRUD — the Cloudinary-HTML "billing template"
 * system (V89), distinct from the untouched, hardcoded InvoiceTemplateController.
 * Real Escape/Deal invoice preview lives on EscapeController (real data,
 * same QuotationRenderingService/QuotationPdfService as Quotation) — kept
 * there rather than here since it needs escape-scoped access checks, not
 * template-scoped ones.
 */
@RestController
@RequestMapping("/api/billing-templates")
@RequiredArgsConstructor
public class BillingTemplateController {

    private final BillingTemplateService billingTemplateService;

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BillingTemplateResponseDTO> create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "previewImage", required = false) MultipartFile previewImage) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingTemplateService.create(name, description, file, previewImage));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping
    public ResponseEntity<List<BillingTemplateResponseDTO>> getAllActive() {
        return ResponseEntity.ok(billingTemplateService.getAllActive());
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping("/{uid}")
    public ResponseEntity<BillingTemplateResponseDTO> getById(@PathVariable UUID uid) {
        return ResponseEntity.ok(billingTemplateService.getById(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PutMapping(value = "/{uid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BillingTemplateResponseDTO> update(
            @PathVariable UUID uid,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "previewImage", required = false) MultipartFile previewImage) {
        BillingTemplateUpdateRequestDTO dto = new BillingTemplateUpdateRequestDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setIsActive(isActive);
        return ResponseEntity.ok(billingTemplateService.update(uid, dto, file, previewImage));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uid) {
        billingTemplateService.delete(uid);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping("/{uid}/set-default")
    public ResponseEntity<Void> setAsDefault(@PathVariable UUID uid) {
        billingTemplateService.setAsDefault(uid);
        return ResponseEntity.noContent().build();
    }
}
