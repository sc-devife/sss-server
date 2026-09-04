package com.sss.app.mapper;

import com.sss.app.dto.organizations.OrganizationSettingsDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.organizations.OrganizationSettings;
import com.sss.app.entity.organizations.Organizations;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {
    public OrganizationsDto toDto(Organizations orgs) {
        return toDto(orgs, null);
    }

    public OrganizationsDto toDto(Organizations orgs, OrganizationSettings settings) {
        if (orgs == null) return null;
        OrganizationsDto dto = new OrganizationsDto();
        dto.setSeqp(orgs.getSeqp());
        dto.setUid(orgs.getUid());
        dto.setRegistered_name(orgs.getRegisteredName());
        dto.setDisplay_name(orgs.getDisplayName());
        dto.setOrg_code(orgs.getOrgCode());
        dto.setSupport_ph_num(orgs.getSupportPhNum());
        dto.setCountry_code(orgs.getCountryCode());
        dto.setLogo_file(orgs.getLogoFile());
        dto.setLogo_shape(orgs.getLogoShape());
        dto.setStatus(orgs.getStatus());
        dto.setPan(orgs.getPan());
        dto.setLegal_entity_type(orgs.getLegalEntityType());
        dto.setCin(orgs.getCin());
        dto.setBusiness_email(orgs.getBusinessEmail());
        dto.setWebsite_url(orgs.getWebsiteUrl());
        dto.setInstagram_url(orgs.getInstagramUrl());
        dto.setLinkedin_url(orgs.getLinkedinUrl());
        dto.setWhatsapp_number(orgs.getWhatsappNumber());
        dto.setTagline(orgs.getTagline());
        dto.setAbout_text(orgs.getAboutText());
        dto.setIndustry_accreditation(orgs.getIndustryAccreditation());
        dto.setCreated_at(orgs.getCreatedAt());
        dto.setUpdated_at(orgs.getUpdatedAt());
        dto.setSettings(toSettingsDto(settings));
        return dto;
    }

    public OrganizationSettingsDto toSettingsDto(OrganizationSettings settings) {
        if (settings == null) return null;
        OrganizationSettingsDto dto = new OrganizationSettingsDto();
        dto.setAuto_assign_enabled(settings.getAutoAssignEnabled());
        dto.setDefault_currency_code(settings.getDefaultCurrencyCode());
        dto.setQuote_template_id(settings.getQuoteTemplateId());
        dto.setInvoice_template_id(settings.getInvoiceTemplateId());
        dto.setDefault_quotation_template_id(settings.getDefaultQuotationTemplateId());
        dto.setTimezone(settings.getTimezone());
        dto.setDefault_locale(settings.getDefaultLocale());
        dto.setDefault_payment_terms_days(settings.getDefaultPaymentTermsDays());
        dto.setBrand_primary_color(settings.getBrandPrimaryColor());
        dto.setCreated_at(settings.getCreatedAt());
        dto.setUpdated_at(settings.getUpdatedAt());
        return dto;
    }
}
