package com.sss.app.repository;

import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organizations, Long> {
    Optional<Organizations> findByRegisteredName(String registeredName);
    Optional<Organizations> findByUid(String uid);
    boolean existsByRegisteredName(String registeredName);
    void deleteByRegisteredName(String registeredName);
}
