package com.sss.app.dto.billingtemplate;

import lombok.Data;

@Data
public class BillingTemplateUpdateRequestDTO {

    private String name;
    private String description;
    private Boolean isActive;
}
