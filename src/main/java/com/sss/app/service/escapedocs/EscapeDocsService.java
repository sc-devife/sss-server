package com.sss.app.service.escapedocs;

import java.util.UUID;

public interface EscapeDocsService {
    String renderHtml(UUID escapeUid, DocSections sections);
    EscapeDocumentResult renderPdf(UUID escapeUid, DocSections sections);
    EscapeDocumentResult renderWord(UUID escapeUid, DocSections sections);
}
