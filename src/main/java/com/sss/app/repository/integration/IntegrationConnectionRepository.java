package com.sss.app.repository.integration;

import com.sss.app.entity.integration.IntegrationConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntegrationConnectionRepository extends JpaRepository<IntegrationConnection, Long> {

    List<IntegrationConnection> findAllByOrgId(Long orgId);

    Optional<IntegrationConnection> findByOrgIdAndChannelCode(Long orgId, String channelCode);
}
