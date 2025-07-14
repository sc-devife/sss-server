package com.sss.app.mapper;

import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.organizations.Organizations;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {
    public OrganizationsDto toDto(Organizations orgs) {
        if (orgs == null) return null;
        return new OrganizationsDto(
                orgs.getSeqp(),
                orgs.getRegisteredName(),
                orgs.getDisplayName(),
                orgs.getSupportPhNum());
    }
}
