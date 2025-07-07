package com.sss.app.service;

import com.sss.app.dto.OrganizationsDto;

public interface OrganizationsService {
    OrganizationsDto getUserByRegisteredName(String orgRegName);
    OrganizationsDto createOrganizations(OrganizationsDto createRequest);
    OrganizationsDto updateOrganizations(OrganizationsDto createRequest);

}
