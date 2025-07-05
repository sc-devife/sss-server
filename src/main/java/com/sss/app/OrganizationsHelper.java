package com.sss.app;

import com.sss.app.entity.Organizations;
import com.sss.app.entity.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class OrganizationsHelper {
    private final OrganizationRepository organizationRepository;

    public OrganizationsHelper(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organizations getUserByRegisteredName(String orgRegName) {
        return organizationRepository.findByRegisteredName(orgRegName)
                .orElseThrow(() -> new NotFoundException("Organization not found with name: " + orgRegName));
    }
}
