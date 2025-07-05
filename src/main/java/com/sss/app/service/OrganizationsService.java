package com.sss.app.service;

import com.sss.app.dto.OrganizationsDto;

public interface OrganizationsService {
    OrganizationsDto getUserByRegisteredName(String orgRegName);

}
