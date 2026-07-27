package com.sss.app.repository;

import com.sss.app.entity.roles.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByNameIn(List<String> names);

    /** Platform roles (orgId null) plus any role scoped to this specific org. */
    List<Role> findByOrgIdIsNullOrOrgId(Long orgId);
}
