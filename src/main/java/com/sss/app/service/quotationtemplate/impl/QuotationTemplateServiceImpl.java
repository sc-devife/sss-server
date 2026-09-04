package com.sss.app.service.quotationtemplate.impl;

import com.sss.app.dto.quotationtemplate.QuotationTemplateResponseDTO;
import com.sss.app.dto.quotationtemplate.QuotationTemplateUpdateRequestDTO;
import com.sss.app.entity.organizations.OrganizationSettings;
import com.sss.app.entity.quotationtemplate.QuotationTemplate;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.helper.OrganizationsHelper;
import com.sss.app.repository.OrganizationSettingsRepository;
import com.sss.app.repository.quotationtemplate.QuotationTemplateRepository;
import com.sss.app.service.files.CloudinaryService;
import com.sss.app.service.files.CloudinaryUploadResult;
import com.sss.app.service.quotationtemplate.QuotationDataService;
import com.sss.app.service.quotationtemplate.QuotationPdfResult;
import com.sss.app.service.quotationtemplate.QuotationPdfService;
import com.sss.app.service.quotationtemplate.QuotationRenderingService;
import com.sss.app.service.quotationtemplate.QuotationTemplateService;
import com.sss.app.service.quotationtemplate.SampleQuotationDataService;
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
public class QuotationTemplateServiceImpl implements QuotationTemplateService {

    private static final String CLOUDINARY_FOLDER = "sss/quotation-templates";

    private final QuotationTemplateRepository quotationTemplateRepository;
    private final OrganizationSettingsRepository organizationSettingsRepository;
    private final OrganizationsHelper organizationsHelper;
    private final CloudinaryService cloudinaryService;
    private final QuotationRenderingService quotationRenderingService;
    private final SampleQuotationDataService sampleQuotationDataService;
    private final QuotationDataService quotationDataService;
    private final QuotationPdfService quotationPdfService;

    @Override
    public QuotationTemplateResponseDTO create(String name, String description, MultipartFile htmlFile, MultipartFile previewImage) {
        if (htmlFile == null || htmlFile.isEmpty()) {
            throw new BadRequestException("A template HTML file is required");
        }
        CloudinaryUploadResult htmlUpload = cloudinaryService.uploadHtml(htmlFile, CLOUDINARY_FOLDER);
        String previewImageUrl = null;
        if (previewImage != null && !previewImage.isEmpty()) {
            previewImageUrl = cloudinaryService.upload(previewImage, CLOUDINARY_FOLDER).secureUrl();
        }
        QuotationTemplate template = QuotationTemplate.builder()
                .name(name)
                .description(description)
                .cloudinaryUrl(htmlUpload.secureUrl())
                .cloudinaryPublicId(htmlUpload.publicId())
                .previewImageUrl(previewImageUrl)
                .isActive(true)
                .build();
        return toResponse(quotationTemplateRepository.save(template));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationTemplateResponseDTO> getAllActive() {
        return quotationTemplateRepository.findAllByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationTemplateResponseDTO getById(UUID uid) {
        return toResponse(findEntity(uid));
    }

    @Override
    public QuotationTemplateResponseDTO update(UUID uid, QuotationTemplateUpdateRequestDTO dto, MultipartFile htmlFile, MultipartFile previewImage) {
        QuotationTemplate template = findEntity(uid);
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
        return toResponse(quotationTemplateRepository.save(template));
    }

    @Override
    public void delete(UUID uid) {
        QuotationTemplate template = findEntity(uid);
        quotationTemplateRepository.delete(template);
        cloudinaryService.deleteByPublicId(template.getCloudinaryPublicId(), "raw");
        cloudinaryService.deleteByUrl(template.getPreviewImageUrl());
    }

    @Override
    @Transactional(readOnly = true)
    public String previewWithSampleData(UUID uid) {
        QuotationTemplate template = findEntity(uid);
        return quotationRenderingService.render(template.getCloudinaryUrl(), sampleQuotationDataService.buildSampleData());
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationPdfResult previewWithSampleDataAsPdf(UUID uid) {
        QuotationTemplate template = findEntity(uid);
        Map<String, Object> data = sampleQuotationDataService.buildSampleData();
        String html = quotationRenderingService.render(template.getCloudinaryUrl(), data);
        byte[] pdf = quotationPdfService.render(html, watermarkText(data));
        // Not a real quotation — no quote number applies, and the filename
        // deliberately avoids the word "Quotation" per the same convention
        // real downloads use.
        return new QuotationPdfResult(pdf, "template-preview.pdf");
    }

    @Override
    public void setAsDefault(UUID uid) {
        QuotationTemplate template = findEntity(uid);
        Long orgId = organizationsHelper.getMyOrganization().getSeqp();
        OrganizationSettings settings = organizationsHelper.getSettings(orgId);
        settings.setDefaultQuotationTemplateId(template.getUid());
        organizationSettingsRepository.save(settings);
    }

    @Override
    @Transactional(readOnly = true)
    public String renderForEscape(UUID escapeUid, UUID templateUid) {
        UUID resolvedTemplateUid = templateUid;
        if (resolvedTemplateUid == null) {
            Long orgId = organizationsHelper.getMyOrganization().getSeqp();
            resolvedTemplateUid = organizationsHelper.getSettings(orgId).getDefaultQuotationTemplateId();
        }
        if (resolvedTemplateUid == null) {
            throw new BadRequestException("No quotation template selected — choose one in Settings > Quotation Templates first");
        }
        QuotationTemplate template = findEntity(resolvedTemplateUid);
        return quotationRenderingService.render(template.getCloudinaryUrl(), quotationDataService.buildData(escapeUid));
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationPdfResult renderForEscapeAsPdf(UUID escapeUid, UUID templateUid) {
        UUID resolvedTemplateUid = templateUid;
        if (resolvedTemplateUid == null) {
            Long orgId = organizationsHelper.getMyOrganization().getSeqp();
            resolvedTemplateUid = organizationsHelper.getSettings(orgId).getDefaultQuotationTemplateId();
        }
        if (resolvedTemplateUid == null) {
            throw new BadRequestException("No quotation template selected — choose one in Settings > Quotation Templates first");
        }
        QuotationTemplate template = findEntity(resolvedTemplateUid);
        Map<String, Object> data = quotationDataService.buildData(escapeUid);
        String html = quotationRenderingService.render(template.getCloudinaryUrl(), data);
        byte[] pdf = quotationPdfService.render(html, watermarkText(data));
        return new QuotationPdfResult(pdf, quoteNameFilename(data));
    }

    // The quote actually rendered (accepted, else latest — see
    // QuotationDataService) is the one whose existing Quote Name — the same
    // name shown in the Quotes section on the Escape page — names the file;
    // no new/separate name is generated here.
    @SuppressWarnings("unchecked")
    private String quoteNameFilename(Map<String, Object> data) {
        Object pricing = data.get("pricing");
        Object quoteName = pricing instanceof Map ? ((Map<String, Object>) pricing).get("quoteName") : null;
        String name = quoteName != null ? quoteName.toString() : "quote";
        // Strip characters that are invalid in a filename on any major OS —
        // the name itself is untouched, only made safe to save as a file.
        String sanitized = name.replaceAll("[\\\\/:*?\"<>|]", "-").trim();
        return (sanitized.isEmpty() ? "quote" : sanitized) + ".pdf";
    }

    // "TRP-000007 · Wanderlust Escapes Pvt Ltd" — same trip code + org name
    // now shown in the quotation header, reused so the watermark can never
    // say something different from the document it's stamped on.
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

    private QuotationTemplate findEntity(UUID uid) {
        return quotationTemplateRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("QuotationTemplate", uid));
    }

    private QuotationTemplateResponseDTO toResponse(QuotationTemplate template) {
        Long orgId = organizationsHelper.getMyOrganization().getSeqp();
        UUID defaultId = organizationsHelper.getSettings(orgId).getDefaultQuotationTemplateId();
        return QuotationTemplateResponseDTO.builder()
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
