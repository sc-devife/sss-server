package com.sss.app.service.quotationtemplate;

/**
 * A generated quotation PDF plus the filename it should be downloaded as
 * (e.g. "QUOTE-00001.pdf") — the backend decides the filename because it's
 * the one place that already knows which quote was actually rendered (the
 * accepted one, or the latest, per QuotationDataService); the frontend has
 * no equivalent way to know this in advance.
 */
public record QuotationPdfResult(byte[] bytes, String filename) {
}
