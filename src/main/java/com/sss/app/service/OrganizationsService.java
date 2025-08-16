package com.sss.app.service;

import com.sss.app.dto.organizations.OrganizationsDto;

public interface OrganizationsService {
    //OrganizationsDto getOrganizationsRegisteredName(String uid);
    OrganizationsDto getOrganizationsByUid(String uid);
    OrganizationsDto createOrganizations(OrganizationsDto createRequest);
    OrganizationsDto updateOrganizations(String uid, OrganizationsDto createRequest);
    void deleteOrganizations(String orgName);
}
