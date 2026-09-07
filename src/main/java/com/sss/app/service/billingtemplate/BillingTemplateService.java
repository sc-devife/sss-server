package com.sss.app.service.billingtemplate;

import com.sss.app.dto.billingtemplate.BillingTemplateResponseDTO;
import com.sss.app.dto.billingtemplate.BillingTemplateUpdateRequestDTO;
import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.service.quotationtemplate.QuotationPdfResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BillingTemplateService {

    BillingTemplateResponseDTO create(String name, String description, MultipartFile htmlFile, MultipartFile previewImage);

    List<BillingTemplateResponseDTO> getAllActive();

    BillingTemplateResponseDTO getById(UUID uid);

    BillingTemplateResponseDTO update(UUID uid, BillingTemplateUpdateRequestDTO dto, MultipartFile htmlFile, MultipartFile previewImage);

    void delete(UUID uid);

    void setAsDefault(UUID uid);

    /** Renders a real Escape's invoice data against templateUid, or the org's default billing template when null. */
    String renderForEscape(UUID escapeUid, UUID templateUid);

    /** Same as {@link #renderForEscape}, as a downloadable watermarked PDF. */
    QuotationPdfResult renderForEscapeAsPdf(UUID escapeUid, UUID templateUid);

    /** Emails the same watermarked PDF as {@link #renderForEscapeAsPdf} to the escape's lead + traveller addresses. */
    SendEmailResponseDTO sendEmailForEscape(UUID escapeUid, UUID templateUid);
}
