package com.sss.app.bulkimport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * One generic upload/validate/commit surface for every Library entity's
 * bulk import (Section 10) — entityType selects the schema (see
 * BulkImportRegistry); nothing else here is entity-specific.
 */
@RestController
@RequestMapping("/api/bulk-import/{entityType}")
@RequiredArgsConstructor
public class BulkImportController {

    private final BulkImportRegistry registry;
    private final SpreadsheetParser spreadsheetParser;

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping(value = "/template", produces = "text/csv")
    public ResponseEntity<byte[]> template(@PathVariable String entityType) throws IOException {
        BulkImportSchema schema = registry.get(entityType);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(schema.columns().toArray(new String[0])).build())) {
            printer.flush();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + entityType + "-template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(out.toByteArray());
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkImportPreviewResponse> preview(@PathVariable String entityType,
                                                               @RequestParam("file") MultipartFile file) {
        BulkImportSchema schema = registry.get(entityType);
        List<Map<String, String>> rows = spreadsheetParser.parse(file);

        List<BulkImportRowResult> results = new ArrayList<>();
        int validCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            List<String> errors = validateRowAgainstSchema(schema, row);
            if (errors.isEmpty()) validCount++;
            results.add(new BulkImportRowResult(i + 1, row, errors));
        }

        return ResponseEntity.ok(new BulkImportPreviewResponse(entityType, rows.size(), validCount, results));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping(value = "/commit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkImportCommitResponse> commit(@PathVariable String entityType,
                                                             @RequestParam("file") MultipartFile file) {
        BulkImportSchema schema = registry.get(entityType);
        List<Map<String, String>> rows = spreadsheetParser.parse(file);

        int committed = 0;
        List<BulkImportRowResult> failedRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            List<String> errors = validateRowAgainstSchema(schema, row);
            if (!errors.isEmpty()) {
                failedRows.add(new BulkImportRowResult(i + 1, row, errors));
                continue;
            }
            try {
                schema.commitRow(row);
                committed++;
            } catch (Exception e) {
                failedRows.add(new BulkImportRowResult(i + 1, row, List.of(e.getMessage() != null ? e.getMessage() : "Failed to import this row")));
            }
        }

        return ResponseEntity.ok(new BulkImportCommitResponse(committed, failedRows.size(), failedRows));
    }

    private List<String> validateRowAgainstSchema(BulkImportSchema schema, Map<String, String> row) {
        List<String> errors = new ArrayList<>();
        for (String required : schema.requiredColumns()) {
            String value = row.get(required);
            if (value == null || value.isBlank()) {
                errors.add("\"" + required + "\" is required");
            }
        }
        errors.addAll(schema.validateRow(row));
        return errors;
    }
}
