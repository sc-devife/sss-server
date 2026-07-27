package com.sss.app.service.permissions;

import com.sss.app.entity.userrolelinks.UserRoleLink;
import com.sss.app.entity.users.User;
import com.sss.app.repository.permissions.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Backs @PreAuthorize("@permissionService.hasPermission('...')") checks —
 * looks up the authenticated user's roles and asks role_permissions whether
 * any of them grant the given key. Adding a new permission or granting it to
 * a role is a data change (see V6__seed_rbac.sql), not a code change.
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    public boolean hasPermission(String key) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return false;
        }

        return user.getRoles().stream()
                .map(UserRoleLink::getRole)
                .filter(Objects::nonNull)
                .flatMap(role -> rolePermissionRepository.findAllByRoleId(role.getSeqp()).stream())
                .anyMatch(rolePermission -> key.equals(rolePermission.getPermission().getKey()));
    }
}
