package com.sss.app.helper;

import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.service.files.CloudinaryService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrganizationsHelper {
    private final OrganizationRepository organizationRepository;
    private final CloudinaryService cloudinaryService;
    @PersistenceContext
    private EntityManager entityManager;

    public OrganizationsHelper(OrganizationRepository organizationRepository, CloudinaryService cloudinaryService) {
        this.organizationRepository = organizationRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public Organizations getMyOrganization() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getOrgId() == null) {
            throw new NotFoundException("You are not associated with an organization yet");
        }
        return organizationRepository.findById(user.getOrgId())
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    public Organizations getOrganizationByUid(String uid) {
        return organizationRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Organization not found with uid: " + uid));

    }
    public Organizations getOrganizationsByUid(String uid) {
        return organizationRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Organization not found with uid: " + uid));
    }
    @Transactional
    public Organizations createOrganizations(OrganizationsDto request) {
        if (organizationRepository.existsByRegisteredName(request.getRegistered_name())) {
            throw new IllegalArgumentException("Registered name already in use");
        }
        Organizations organization = Organizations.create(request);
        organization.setOrgCode(generateOrgCode(request.getDisplay_name()));
        organization = organizationRepository.save(organization);
        entityManager.flush();
        entityManager.refresh(organization);
        return organization;
    }

    /** Slug derived from display_name, uniquified with a numeric suffix on collision. */
    private String generateOrgCode(String displayName) {
        String base = displayName == null ? "" : displayName.toLowerCase().trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (base.isBlank()) {
            base = "org";
        }
        String candidate = base;
        int suffix = 2;
        while (organizationRepository.existsByOrgCode(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }
    @Transactional
    public Organizations updateOrganizations(String uid, OrganizationsDto request) {
        Organizations organization = getOrganizationByUid(uid);
        String previousLogoFile = organization.getLogoFile();
        organization.update(request);
        organizationRepository.save(organization);
        entityManager.flush();
        entityManager.refresh(organization);

        if (!Objects.equals(previousLogoFile, organization.getLogoFile())) {
            cloudinaryService.deleteByUrl(previousLogoFile);
        }

        return organization;
    }

    @Transactional
    public void deleteOrganizations(String orgRegName) {
        organizationRepository.findByRegisteredName(orgRegName)
                .orElseThrow(() -> new NotFoundException("Organization not found with name: " + orgRegName));
        organizationRepository.deleteByRegisteredName(orgRegName);
    }
}
