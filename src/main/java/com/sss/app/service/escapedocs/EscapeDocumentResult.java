package com.sss.app.service.escapedocs;

/**
 * A generated Escape Document (PDF or Word) plus the filename it should be
 * downloaded as — same shape as QuotationPdfResult, reused for both formats
 * since each is just "generated file bytes + a filename".
 */
public record EscapeDocumentResult(byte[] bytes, String filename) {
}
