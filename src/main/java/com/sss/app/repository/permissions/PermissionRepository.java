package com.sss.app.repository.permissions;

import com.sss.app.entity.permissions.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByKey(String key);
}
