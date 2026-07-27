package com.sss.app.bulkimport;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class BulkImportRowResult {
    private int rowNumber; // 1-based, excluding the header row
    private Map<String, String> data;
    private List<String> errors;
}
