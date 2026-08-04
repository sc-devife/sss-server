package com.sss.app.dto.integration.meta;

import lombok.Data;

@Data
public class MetaFieldMappingDTO {
    private Long seqp;
    private String formId; // null = org-wide default
    private String metaFieldKey;
    private String crmField;
}
