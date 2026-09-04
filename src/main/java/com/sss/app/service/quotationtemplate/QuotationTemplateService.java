package com.sss.app.service.quotationtemplate;

import com.sss.app.dto.quotationtemplate.QuotationTemplateResponseDTO;
import com.sss.app.dto.quotationtemplate.QuotationTemplateUpdateRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface QuotationTemplateService {

    QuotationTemplateResponseDTO create(String name, String description, MultipartFile htmlFile, MultipartFile previewImage);

    List<QuotationTemplateResponseDTO> getAllActive();

    QuotationTemplateResponseDTO getById(UUID uid);

    QuotationTemplateResponseDTO update(UUID uid, QuotationTemplateUpdateRequestDTO dto, MultipartFile htmlFile, MultipartFile previewImage);

    void delete(UUID uid);

    /** Renders this template against predefined sample data — Settings preview, no Escape required. */
    String previewWithSampleData(UUID uid);

    /** Same as {@link #previewWithSampleData}, as a downloadable watermarked PDF. */
    QuotationPdfResult previewWithSampleDataAsPdf(UUID uid);

    void setAsDefault(UUID uid);

    /** Renders real Escape/quotation data against templateUid, or the org's default template when null. */
    String renderForEscape(UUID escapeUid, UUID templateUid);

    /** Same as {@link #renderForEscape}, as a downloadable watermarked PDF. */
    QuotationPdfResult renderForEscapeAsPdf(UUID escapeUid, UUID templateUid);
}
