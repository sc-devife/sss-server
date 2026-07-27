package com.sss.app.bulkimport;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkImportCommitResponse {
    private int committed;
    private int failed;
    private List<BulkImportRowResult> failedRows; // only rows that failed, with their errors
}
