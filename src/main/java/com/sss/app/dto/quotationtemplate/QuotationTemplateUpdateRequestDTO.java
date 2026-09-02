package com.sss.app.dto.quotationtemplate;

import lombok.Data;

@Data
public class QuotationTemplateUpdateRequestDTO {

    private String name;
    private String description;
    private Boolean isActive;
}
