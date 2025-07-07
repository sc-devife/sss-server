package com.sss.app.service.impl;

import com.sss.app.OrganizationsHelper;
import com.sss.app.UsersHelper;
import com.sss.app.dto.OrganizationsDto;
import com.sss.app.dto.UserDto;
import com.sss.app.entity.Organizations;
import com.sss.app.entity.User;
import com.sss.app.mapper.OrganizationMapper;
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
    public OrganizationsDto getUserByRegisteredName(String orgRegName) {
        Organizations orgs = organizationsHelper.getUserByRegisteredName(orgRegName);
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
}
