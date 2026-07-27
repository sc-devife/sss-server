package com.sss.app.security;

import com.sss.app.entity.users.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Every org-scoped endpoint that takes an orgId as a request parameter needs
 * this check — without it, orgId is fully client-controlled and any
 * authenticated user could read/write another organization's data just by
 * changing the id in the URL. Super Admins operate across all orgs by design
 * (Section 2) and skip the check.
 */
@Component
public class OrgAccessGuard {

    public void requireAccessToOrg(Long orgId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Not authenticated");
        }

        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return;
        }

        if (user.getOrgId() == null || !user.getOrgId().equals(orgId)) {
            throw new AccessDeniedException("You do not have access to this organization's data");
        }
    }
}
