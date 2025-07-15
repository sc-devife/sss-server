package com.sss.app.service.impl;

import com.sss.app.OrganizationsHelper;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.OrganizationMapper;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.service.OrganizationsService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationsService {
    OrganizationsHelper organizationsHelper;
    OrganizationMapper organizationMapper;
    OrganizationRepository organizationRepository;

    public OrganizationServiceImpl(OrganizationsHelper organizationsHelper, OrganizationMapper organizationMapper) {
        this.organizationsHelper = organizationsHelper;
        this.organizationMapper = organizationMapper;
    }

    @Override
    public OrganizationsDto getOrganizationsRegisteredName(String orgRegName) {
        Organizations orgs = organizationsHelper.getOrganizationsRegisteredName(orgRegName);
        return organizationMapper.toDto(orgs);
    }

    @Override
    public OrganizationsDto createOrganizations(OrganizationsDto createRequest) {
        Organizations orgs = organizationsHelper.createOrganizations(createRequest);
        return organizationMapper.toDto(orgs);
    }

    @Override
    public OrganizationsDto updateOrganizations(OrganizationsDto createRequest) {
        Organizations orgs = organizationsHelper.updateOrganizations(createRequest);
        return organizationMapper.toDto(orgs);
    }
    public void deleteOrganizations(String orgRegName) {
        organizationsHelper.deleteOrganizations(orgRegName);
    }

}
