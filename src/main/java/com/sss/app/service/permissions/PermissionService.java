package com.sss.app.service.permissions;

import com.sss.app.entity.roles.Role;
import com.sss.app.entity.userrolelinks.UserRoleLink;
import com.sss.app.entity.users.User;
import com.sss.app.repository.permissions.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
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

        List<Long> roleIds = user.getRoles().stream()
                .map(UserRoleLink::getRole)
                .filter(Objects::nonNull)
                .map(Role::getSeqp)
                .toList();
        if (roleIds.isEmpty()) {
            return false;
        }

        // One query regardless of how many roles the user holds, instead of
        // one query per role — this runs on every single @PreAuthorize check
        // across the whole app, so it's worth not paying N round-trips here.
        return rolePermissionRepository.findAllByRoleIdIn(roleIds).stream()
                .anyMatch(rolePermission -> key.equals(rolePermission.getPermission().getKey()));
    }
}
