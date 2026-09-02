package com.sss.app.dto.organizations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSettingsDto {
    private Boolean auto_assign_enabled;
    private String default_currency_code;
    private UUID quote_template_id;
    private UUID invoice_template_id;
    private UUID default_quotation_template_id;
    private String timezone;
    private String default_locale;
    private Integer default_payment_terms_days;
    private String brand_primary_color;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
