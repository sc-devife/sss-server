package com.sss.app.service.billingtemplate.impl;

import com.sss.app.dto.billingtemplate.BillingTemplateResponseDTO;
import com.sss.app.dto.billingtemplate.BillingTemplateUpdateRequestDTO;
import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.entity.billingtemplate.BillingTemplate;
import com.sss.app.entity.organizations.OrganizationSettings;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.helper.OrganizationsHelper;
import com.sss.app.repository.OrganizationSettingsRepository;
import com.sss.app.repository.billingtemplate.BillingTemplateRepository;
import com.sss.app.service.billingtemplate.BillingDataService;
import com.sss.app.service.billingtemplate.BillingTemplateService;
import com.sss.app.service.email.EmailService;
import com.sss.app.service.email.EscapeEmailRecipientResolver;
import com.sss.app.service.files.CloudinaryService;
import com.sss.app.service.files.CloudinaryUploadResult;
import com.sss.app.service.quotationtemplate.QuotationPdfResult;
import com.sss.app.service.quotationtemplate.QuotationPdfService;
import com.sss.app.service.quotationtemplate.QuotationRenderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingTemplateServiceImpl implements BillingTemplateService {

    private static final String CLOUDINARY_FOLDER = "sss/invoice-templates";
    private static final String EMAIL_BODY_TEMPLATE = "email-templates/invoice-email.mustache";
    private static final String EMAIL_SUBJECT_TEMPLATE = "Invoice {{invoice.invoiceNumber}} — {{organization.name}}";

    private final BillingTemplateRepository billingTemplateRepository;
    private final OrganizationSettingsRepository organizationSettingsRepository;
    private final OrganizationsHelper organizationsHelper;
    private final CloudinaryService cloudinaryService;
    private final QuotationRenderingService quotationRenderingService;
    private final BillingDataService billingDataService;
    private final QuotationPdfService quotationPdfService;
    private final EmailService emailService;
    private final EscapeEmailRecipientResolver escapeEmailRecipientResolver;

    @Override
    public BillingTemplateResponseDTO create(String name, String description, MultipartFile htmlFile, MultipartFile previewImage) {
        if (htmlFile == null || htmlFile.isEmpty()) {
            throw new BadRequestException("A template HTML file is required");
        }
        CloudinaryUploadResult htmlUpload = cloudinaryService.uploadHtml(htmlFile, CLOUDINARY_FOLDER);
        String previewImageUrl = null;
        if (previewImage != null && !previewImage.isEmpty()) {
            previewImageUrl = cloudinaryService.upload(previewImage, CLOUDINARY_FOLDER).secureUrl();
        }
        BillingTemplate template = BillingTemplate.builder()
                .name(name)
                .description(description)
                .cloudinaryUrl(htmlUpload.secureUrl())
                .cloudinaryPublicId(htmlUpload.publicId())
                .previewImageUrl(previewImageUrl)
                .isActive(true)
                .build();
        return toResponse(billingTemplateRepository.save(template));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingTemplateResponseDTO> getAllActive() {
        return billingTemplateRepository.findAllByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BillingTemplateResponseDTO getById(UUID uid) {
        return toResponse(findEntity(uid));
    }

    @Override
    public BillingTemplateResponseDTO update(UUID uid, BillingTemplateUpdateRequestDTO dto, MultipartFile htmlFile, MultipartFile previewImage) {
        BillingTemplate template = findEntity(uid);
        if (dto != null) {
            if (dto.getName() != null) template.setName(dto.getName());
            if (dto.getDescription() != null) template.setDescription(dto.getDescription());
            if (dto.getIsActive() != null) template.setIsActive(dto.getIsActive());
        }
        if (htmlFile != null && !htmlFile.isEmpty()) {
            String previousPublicId = template.getCloudinaryPublicId();
            CloudinaryUploadResult upload = cloudinaryService.uploadHtml(htmlFile, CLOUDINARY_FOLDER);
            template.setCloudinaryUrl(upload.secureUrl());
            template.setCloudinaryPublicId(upload.publicId());
            cloudinaryService.deleteByPublicId(previousPublicId, "raw");
        }
        if (previewImage != null && !previewImage.isEmpty()) {
            String previousPreview = template.getPreviewImageUrl();
            template.setPreviewImageUrl(cloudinaryService.upload(previewImage, CLOUDINARY_FOLDER).secureUrl());
            cloudinaryService.deleteByUrl(previousPreview);
        }
        return toResponse(billingTemplateRepository.save(template));
    }

    @Override
    public void delete(UUID uid) {
        BillingTemplate template = findEntity(uid);
        billingTemplateRepository.delete(template);
        cloudinaryService.deleteByPublicId(template.getCloudinaryPublicId(), "raw");
        cloudinaryService.deleteByUrl(template.getPreviewImageUrl());
    }

    @Override
    public void setAsDefault(UUID uid) {
        BillingTemplate template = findEntity(uid);
        Long orgId = organizationsHelper.getMyOrganization().getSeqp();
        OrganizationSettings settings = organizationsHelper.getSettings(orgId);
        settings.setDefaultBillingTemplateId(template.getUid());
        organizationSettingsRepository.save(settings);
    }

    @Override
    @Transactional(readOnly = true)
    public String renderForEscape(UUID escapeUid, UUID templateUid) {
        BillingTemplate template = resolveTemplate(templateUid);
        return quotationRenderingService.render(template.getCloudinaryUrl(), billingDataService.buildData(escapeUid));
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationPdfResult renderForEscapeAsPdf(UUID escapeUid, UUID templateUid) {
        BillingTemplate template = resolveTemplate(templateUid);
        Map<String, Object> data = billingDataService.buildData(escapeUid);
        String html = quotationRenderingService.render(template.getCloudinaryUrl(), data);
        byte[] pdf = quotationPdfService.render(html, watermarkText(data));
        return new QuotationPdfResult(pdf, invoiceNumberFilename(data));
    }

    @Override
    @Transactional(readOnly = true)
    public SendEmailResponseDTO sendEmailForEscape(UUID escapeUid, UUID templateUid) {
        BillingTemplate template = resolveTemplate(templateUid);
        Map<String, Object> data = billingDataService.buildData(escapeUid);
        // Resolved before rendering the PDF (a headless-Chrome render) so a
        // recipient-less escape fails fast/cheap instead of after that work.
        List<String> recipients = escapeEmailRecipientResolver.resolveFromRenderedData(data);

        String html = quotationRenderingService.render(template.getCloudinaryUrl(), data);
        byte[] pdf = quotationPdfService.render(html, watermarkText(data));

        String subject = quotationRenderingService.renderInline(EMAIL_SUBJECT_TEMPLATE, data);
        String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
        emailService.sendHtmlEmailWithAttachment(recipients, subject, body, pdf, invoiceNumberFilename(data));

        return new SendEmailResponseDTO(recipients);
    }

    private BillingTemplate resolveTemplate(UUID templateUid) {
        UUID resolvedTemplateUid = templateUid;
        if (resolvedTemplateUid == null) {
            Long orgId = organizationsHelper.getMyOrganization().getSeqp();
            resolvedTemplateUid = organizationsHelper.getSettings(orgId).getDefaultBillingTemplateId();
        }
        if (resolvedTemplateUid == null) {
            resolvedTemplateUid = billingTemplateRepository.findAllByIsActiveTrue().stream()
                    .findFirst()
                    .map(BillingTemplate::getUid)
                    .orElse(null);
        }
        if (resolvedTemplateUid == null) {
            throw new BadRequestException("No invoice template is available yet — add one first");
        }
        return findEntity(resolvedTemplateUid);
    }

    // Same "tripCode · orgName" convention QuotationTemplateServiceImpl uses,
    // reused so the watermark can't say something different from the
    // document it's stamped on.
    @SuppressWarnings("unchecked")
    private String watermarkText(Map<String, Object> data) {
        Object tripCode = data.get("tripCode");
        Object organization = data.get("organization");
        Object orgName = organization instanceof Map ? ((Map<String, Object>) organization).get("name") : null;
        if (tripCode == null && orgName == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (tripCode != null) sb.append(tripCode);
        if (tripCode != null && orgName != null) sb.append(" · ");
        if (orgName != null) sb.append(orgName);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String invoiceNumberFilename(Map<String, Object> data) {
        Object invoice = data.get("invoice");
        Object invoiceNumber = invoice instanceof Map ? ((Map<String, Object>) invoice).get("invoiceNumber") : null;
        String name = invoiceNumber != null ? invoiceNumber.toString() : "invoice";
        String sanitized = name.replaceAll("[\\\\/:*?\"<>|]", "-").trim();
        return (sanitized.isEmpty() ? "invoice" : sanitized) + ".pdf";
    }

    private BillingTemplate findEntity(UUID uid) {
        return billingTemplateRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("BillingTemplate", uid));
    }

    private BillingTemplateResponseDTO toResponse(BillingTemplate template) {
        Long orgId = organizationsHelper.getMyOrganization().getSeqp();
        UUID defaultId = organizationsHelper.getSettings(orgId).getDefaultBillingTemplateId();
        return BillingTemplateResponseDTO.builder()
                .uid(template.getUid())
                .name(template.getName())
                .description(template.getDescription())
                .cloudinaryUrl(template.getCloudinaryUrl())
                .previewImageUrl(template.getPreviewImageUrl())
                .isActive(template.getIsActive())
                .isDefault(template.getUid().equals(defaultId))
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
