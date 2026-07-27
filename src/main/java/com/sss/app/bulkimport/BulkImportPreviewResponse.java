package com.sss.app.bulkimport;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkImportPreviewResponse {
    private String entityType;
    private int totalRows;
    private int validRowCount;
    private List<BulkImportRowResult> rows;
}
