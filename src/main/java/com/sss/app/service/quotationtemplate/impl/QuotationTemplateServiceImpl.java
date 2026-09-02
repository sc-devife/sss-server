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
import com.sss.app.service.quotationtemplate.QuotationRenderingService;
import com.sss.app.service.quotationtemplate.QuotationTemplateService;
import com.sss.app.service.quotationtemplate.SampleQuotationDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
