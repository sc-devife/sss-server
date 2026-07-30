package com.sss.app.mapper;

import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.organizations.Organizations;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {
    public OrganizationsDto toDto(Organizations orgs) {
        if (orgs == null) return null;
        OrganizationsDto dto = new OrganizationsDto();
        dto.setSeqp(orgs.getSeqp());
        dto.setUid(orgs.getUid());
        dto.setRegistered_name(orgs.getRegisteredName());
        dto.setDisplay_name(orgs.getDisplayName());
        dto.setOrg_code(orgs.getOrgCode());
        dto.setSupport_ph_num(orgs.getSupportPhNum());
        dto.setCountry_code(orgs.getCountryCode());
        dto.setDefault_currency_code(orgs.getDefaultCurrencyCode());
        dto.setLogo_file(orgs.getLogoFile());
        dto.setStatus(orgs.getStatus());
        dto.setQuote_template_id(orgs.getQuoteTemplateId());
        dto.setInvoice_template_id(orgs.getInvoiceTemplateId());
        dto.setAuto_assign_enabled(orgs.getAutoAssignEnabled());
        dto.setCreated_at(orgs.getCreatedAt());
        dto.setUpdated_at(orgs.getUpdatedAt());
        return dto;
    }
}
