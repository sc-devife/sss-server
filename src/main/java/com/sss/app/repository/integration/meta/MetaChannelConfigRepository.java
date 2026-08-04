package com.sss.app.repository.integration.meta;

import com.sss.app.entity.integration.meta.MetaChannelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetaChannelConfigRepository extends JpaRepository<MetaChannelConfig, Long> {

    Optional<MetaChannelConfig> findByConnectionId(Long connectionId);

    Optional<MetaChannelConfig> findByPageId(String pageId);

    Optional<MetaChannelConfig> findByIgAccountId(String igAccountId);
}
