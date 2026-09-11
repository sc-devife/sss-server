package com.sss.app.helper;

import com.sss.app.dto.organizations.OrganizationSettingsDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.organizations.OrganizationSettings;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.OrganizationSettingsRepository;
import com.sss.app.security.OrgAccessGuard;
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
    private final OrganizationSettingsRepository organizationSettingsRepository;
    private final CloudinaryService cloudinaryService;
    private final OrgAccessGuard orgAccessGuard;
    @PersistenceContext
    private EntityManager entityManager;

    public OrganizationsHelper(OrganizationRepository organizationRepository,
                                OrganizationSettingsRepository organizationSettingsRepository,
                                CloudinaryService cloudinaryService,
                                OrgAccessGuard orgAccessGuard) {
        this.organizationRepository = organizationRepository;
        this.organizationSettingsRepository = organizationSettingsRepository;
        this.cloudinaryService = cloudinaryService;
        this.orgAccessGuard = orgAccessGuard;
    }

    public OrganizationSettings getSettings(Long orgId) {
        return organizationSettingsRepository.findById(orgId)
                .orElseGet(() -> organizationSettingsRepository.save(OrganizationSettings.createDefault(orgId)));
    }

    public Organizations getMyOrganization() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getOrgId() == null) {
            throw new NotFoundException("You are not associated with an organization yet");
        }
        return organizationRepository.findById(user.getOrgId())
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    // Both "get...ByUid" methods below are read/mutate entry points that take
    // a caller-supplied uid — without requireAccessToOrg, orgId is fully
    // client-controlled and any authenticated user (from any org) could
    // read/edit/delete another organization's record just by changing the
    // uid in the request. Super Admins bypass by design (OrgAccessGuard).
    public Organizations getOrganizationByUid(String uid) {
        Organizations organization = organizationRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Organization not found with uid: " + uid));
        orgAccessGuard.requireAccessToOrg(organization.getSeqp());
        return organization;
    }
    public Organizations getOrganizationsByUid(String uid) {
        Organizations organization = organizationRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Organization not found with uid: " + uid));
        orgAccessGuard.requireAccessToOrg(organization.getSeqp());
        return organization;
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
        organizationSettingsRepository.save(OrganizationSettings.createDefault(organization.getSeqp()));
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

    // Behavioral/config updates go through here, not updateOrganizations() —
    // dedicated /organizations/settings endpoint, dedicated table.
    @Transactional
    public OrganizationSettings updateSettings(Long orgId, OrganizationSettingsDto request) {
        OrganizationSettings settings = getSettings(orgId);
        settings.update(request);
        return organizationSettingsRepository.save(settings);
    }

    @Transactional
    public void deleteOrganizations(String orgRegName) {
        Organizations organization = organizationRepository.findByRegisteredName(orgRegName)
                .orElseThrow(() -> new NotFoundException("Organization not found with name: " + orgRegName));
        orgAccessGuard.requireAccessToOrg(organization.getSeqp());
        organizationRepository.deleteByRegisteredName(orgRegName);
    }
}
