package com.sss.app.service.escapedocs;

import com.sss.app.dto.email.SendEmailResponseDTO;

import java.util.UUID;

public interface EscapeDocsService {
    String renderHtml(UUID escapeUid, DocSections sections);
    EscapeDocumentResult renderPdf(UUID escapeUid, DocSections sections);
    EscapeDocumentResult renderWord(UUID escapeUid, DocSections sections);
    // Emails the same PDF renderPdf() would download to the escape's primary
    // traveller only (never the lead or any other traveller) — see
    // EscapeDocsServiceImpl for why this differs from the Quotation/Invoice
    // "send to everyone" emails.
    SendEmailResponseDTO sendEmail(UUID escapeUid, DocSections sections);
}
