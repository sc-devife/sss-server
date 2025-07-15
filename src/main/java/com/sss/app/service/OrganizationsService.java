package com.sss.app.service;

import com.sss.app.dto.organizations.OrganizationsDto;

public interface OrganizationsService {
    OrganizationsDto getOrganizationsRegisteredName(String orgRegName);
    OrganizationsDto createOrganizations(OrganizationsDto createRequest);
    OrganizationsDto updateOrganizations(OrganizationsDto createRequest);
    void deleteOrganizations(String orgName);
}
