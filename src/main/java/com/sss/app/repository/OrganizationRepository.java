package com.sss.app.repository;

import com.sss.app.entity.organizations.Organizations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organizations, Long> {
    Optional<Organizations> findByRegisteredName(String registeredName);
    boolean existsByRegisteredName(String registeredName);
    void deleteByRegisteredName(String registeredName);

}
