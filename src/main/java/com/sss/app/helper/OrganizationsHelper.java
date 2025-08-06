package com.sss.app.helper;

import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.OrganizationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class OrganizationsHelper {
    private final OrganizationRepository organizationRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public OrganizationsHelper(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }
    public Organizations getOrganizationByUid(String uid) {
        return organizationRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Organization not found with uid: " + uid));

    }
    public Organizations getOrganizationsRegisteredName(String orgRegName) {
        return organizationRepository.findByRegisteredName(orgRegName)
                .orElseThrow(() -> new NotFoundException("Organization not found with name: " + orgRegName));
    }
    @Transactional
    public Organizations createOrganizations(OrganizationsDto request) {
        if (organizationRepository.existsByRegisteredName(request.getRegistered_name())) {
            throw new IllegalArgumentException("Registered name already in use");
        }
        Organizations organization = Organizations.create(request);
        organization = organizationRepository.save(organization);
        entityManager.flush();
        entityManager.refresh(organization);
        return organization;
    }
    @Transactional
    public Organizations updateOrganizations(String uid, OrganizationsDto request) {
        Organizations organization = getOrganizationByUid(uid);
        organization.update(request);
        organizationRepository.save(organization);
        entityManager.flush();
        entityManager.refresh(organization);
        return organization;
    }

    @Transactional
    public void deleteOrganizations(String orgRegName) {
        organizationRepository.findByRegisteredName(orgRegName)
                .orElseThrow(() -> new NotFoundException("Organization not found with name: " + orgRegName));
        organizationRepository.deleteByRegisteredName(orgRegName);
    }
}
