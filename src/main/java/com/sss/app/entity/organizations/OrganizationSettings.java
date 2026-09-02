package com.sss.app.entity.organizations;

import com.sss.app.dto.organizations.OrganizationSettingsDto;
import com.sss.app.util.CompareUtil;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

// Behavioral/configuration settings, split out of Organizations (V58) so
// future settings (notification prefs, reminder cadences, etc.) have a
// dedicated home instead of piling onto the profile row. 1:1 with
// Organizations via a shared primary key (org_id is both PK and FK).
@Entity
@Table(name = "organization_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OrganizationSettings {

    @Id
    @Column(name = "org_id")
    private Long orgId;

    @Builder.Default
    private Boolean autoAssignEnabled = true;

    private String defaultCurrencyCode;

    // Templates don't exist as their own tables yet (Phase 5/6) — these are
    // forward-looking columns only, no FK until that table is built.
    private UUID quoteTemplateId;
    private UUID invoiceTemplateId;

    // Cloudinary-HTML quotation template system (V81) — distinct from
    // quoteTemplateId above (the older hardcoded-list system); this one has
    // a real FK-able table (quotation_templates).
    private UUID defaultQuotationTemplateId;

    @Builder.Default
    private String timezone = "UTC";

    @Builder.Default
    private String defaultLocale = "en";

    private Integer defaultPaymentTermsDays;

    // Document/branding accent color — a styling default, not identity,
    // hence living here rather than on Organizations (V68).
    private String brandPrimaryColor;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static OrganizationSettings createDefault(Long orgId) {
        return OrganizationSettings.builder().orgId(orgId).build();
    }

    public void update(OrganizationSettingsDto dto) {
        if (dto.getAuto_assign_enabled() != null && CompareUtil.hasChanged(dto.getAuto_assign_enabled(), this.autoAssignEnabled)) {
            this.autoAssignEnabled = dto.getAuto_assign_enabled();
        }
        if (dto.getDefault_currency_code() != null && CompareUtil.hasChanged(dto.getDefault_currency_code(), this.defaultCurrencyCode)) {
            this.defaultCurrencyCode = dto.getDefault_currency_code();
        }
        if (dto.getQuote_template_id() != null && CompareUtil.hasChanged(dto.getQuote_template_id(), this.quoteTemplateId)) {
            this.quoteTemplateId = dto.getQuote_template_id();
        }
        if (dto.getInvoice_template_id() != null && CompareUtil.hasChanged(dto.getInvoice_template_id(), this.invoiceTemplateId)) {
            this.invoiceTemplateId = dto.getInvoice_template_id();
        }
        if (dto.getDefault_quotation_template_id() != null && CompareUtil.hasChanged(dto.getDefault_quotation_template_id(), this.defaultQuotationTemplateId)) {
            this.defaultQuotationTemplateId = dto.getDefault_quotation_template_id();
        }
        if (dto.getTimezone() != null && CompareUtil.hasChanged(dto.getTimezone(), this.timezone)) {
            this.timezone = dto.getTimezone();
        }
        if (dto.getDefault_locale() != null && CompareUtil.hasChanged(dto.getDefault_locale(), this.defaultLocale)) {
            this.defaultLocale = dto.getDefault_locale();
        }
        if (dto.getDefault_payment_terms_days() != null && CompareUtil.hasChanged(dto.getDefault_payment_terms_days(), this.defaultPaymentTermsDays)) {
            this.defaultPaymentTermsDays = dto.getDefault_payment_terms_days();
        }
        if (dto.getBrand_primary_color() != null && CompareUtil.hasChanged(dto.getBrand_primary_color(), this.brandPrimaryColor)) {
            this.brandPrimaryColor = dto.getBrand_primary_color();
        }
    }
}
