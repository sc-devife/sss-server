package com.sss.app;

import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.OrganizationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class OrganizationsHelper {
    private final OrganizationRepository organizationRepository;

    public OrganizationsHelper(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organizations getOrganizationsRegisteredName(String orgRegName) {
        return organizationRepository.findByRegisteredName(orgRegName)
                .orElseThrow(() -> new NotFoundException("Organization not found with name: " + orgRegName));
    }

    public Organizations createOrganizations(OrganizationsDto request) {
        if (organizationRepository.existsByRegisteredName(request.getRegistered_name())) {
            throw new IllegalArgumentException("Registered name already in use");
        }
        Organizations organization = Organizations.builder()
                .registeredName(request.getRegistered_name())
                .displayName(request.getDisplay_name())
                .build();
        return organizationRepository.save(organization);
    }

    public Organizations updateOrganizations(OrganizationsDto request) {

        Organizations org = organizationRepository.findByRegisteredName(request.getRegistered_name())
                .orElseThrow(() -> new NotFoundException("Organization not found with name: " + request.getRegistered_name()));

        if (request.getRegistered_name() != null) {
            org.setRegisteredName(request.getRegistered_name());
        }
        org.setDisplayName(request.getDisplay_name());

        return organizationRepository.save(org);

    }
    @Transactional
    public void deleteOrganizations(String orgRegName) {
        organizationRepository.findByRegisteredName(orgRegName)
                .orElseThrow(() -> new NotFoundException("Organization not found with name: " + orgRegName));
        organizationRepository.deleteByRegisteredName(orgRegName);
    }
}
