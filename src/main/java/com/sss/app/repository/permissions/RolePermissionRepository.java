package com.sss.app.repository.permissions;

import com.sss.app.entity.permissions.RolePermission;
import com.sss.app.entity.permissions.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    @Query("""
            SELECT rp FROM RolePermission rp
            LEFT JOIN FETCH rp.permission
            WHERE rp.role.seqp = :roleId
            """)
    List<RolePermission> findAllByRoleId(@Param("roleId") Long roleId);
}
