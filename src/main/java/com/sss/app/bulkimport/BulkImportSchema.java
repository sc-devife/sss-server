package com.sss.app.bulkimport;

import java.util.List;
import java.util.Map;

/**
 * One implementation per Library entity (Section 10): a generic upload ->
 * validate -> show row errors -> confirm -> commit pipeline, where adding
 * bulk import for a new entity means implementing this interface, not
 * building a new importer/controller/upload-UI from scratch.
 */
public interface BulkImportSchema {

    /** URL-safe slug, matches the frontend route segment (e.g. "destinations"). */
    String entityType();

    /** Column headers, in order — used for both the downloadable template and the expected upload shape. */
    List<String> columns();

    List<String> requiredColumns();

    /** Returns validation error messages for this row; empty list means the row is valid. Row values are raw strings from the sheet, keyed by column header. */
    List<String> validateRow(Map<String, String> row);

    /** Creates the entity from an already-validated row (org resolved from the authenticated principal). */
    void commitRow(Map<String, String> row);
}
