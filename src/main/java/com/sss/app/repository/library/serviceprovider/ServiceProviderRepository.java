package com.sss.app.repository.library.serviceprovider;

import com.sss.app.entity.library.serviceprovider.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {

    Optional<ServiceProvider> findByUid(UUID uid);

    List<ServiceProvider> findAllByOrgIdAndDeletedAtIsNull(Long orgId);
}
