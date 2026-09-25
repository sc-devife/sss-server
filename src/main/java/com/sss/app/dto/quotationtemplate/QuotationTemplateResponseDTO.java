package com.sss.app.dto.quotationtemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationTemplateResponseDTO {

    private UUID uid;
    private String name;
    private String description;
    private String cloudinaryUrl;
    private String previewImageUrl;
    private Boolean isActive;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Set only on upload/replace: problems found in the uploaded HTML (e.g. a hard-coded currency symbol).
    private java.util.List<String> warnings;
}
