package com.sss.app.mapper;

import com.sss.app.dto.OrganizationsDto;
import com.sss.app.entity.Organizations;
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
