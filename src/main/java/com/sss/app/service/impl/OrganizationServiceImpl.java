package com.sss.app.service.impl;

import com.sss.app.dto.organizations.OrganizationSettingsDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.helper.OrganizationsHelper;
import com.sss.app.mapper.OrganizationMapper;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.service.OrganizationsService;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationsService {
    OrganizationsHelper organizationsHelper;
    OrganizationMapper organizationMapper;

    public OrganizationServiceImpl(OrganizationsHelper organizationsHelper, OrganizationMapper organizationMapper) {
        this.organizationsHelper = organizationsHelper;
        this.organizationMapper = organizationMapper;
    }

    @Override
    public OrganizationsDto getMyOrganization() {
        Organizations orgs = organizationsHelper.getMyOrganization();
        return organizationMapper.toDto(orgs, organizationsHelper.getSettings(orgs.getSeqp()));
    }

    @Override
    public OrganizationsDto getOrganizationsByUid(String orgRegName) {
        Organizations orgs = organizationsHelper.getOrganizationsByUid(orgRegName);
        return organizationMapper.toDto(orgs, organizationsHelper.getSettings(orgs.getSeqp()));
    }

    @Override
    public OrganizationsDto createOrganizations(OrganizationsDto createRequest) {
        Organizations orgs = organizationsHelper.createOrganizations(createRequest);
        return organizationMapper.toDto(orgs, organizationsHelper.getSettings(orgs.getSeqp()));
    }

    @Override
    public OrganizationsDto updateOrganizations(String uid, OrganizationsDto createRequest) {
        Organizations orgs = organizationsHelper.updateOrganizations(uid, createRequest);
        return organizationMapper.toDto(orgs, organizationsHelper.getSettings(orgs.getSeqp()));
    }
    public void deleteOrganizations(String orgRegName) {
        organizationsHelper.deleteOrganizations(orgRegName);
    }

    @Override
    public OrganizationSettingsDto getMySettings() {
        Organizations orgs = organizationsHelper.getMyOrganization();
        return organizationMapper.toSettingsDto(organizationsHelper.getSettings(orgs.getSeqp()));
    }

    @Override
    public OrganizationSettingsDto updateMySettings(OrganizationSettingsDto request) {
        Organizations orgs = organizationsHelper.getMyOrganization();
        return organizationMapper.toSettingsDto(organizationsHelper.updateSettings(orgs.getSeqp(), request));
    }
}
