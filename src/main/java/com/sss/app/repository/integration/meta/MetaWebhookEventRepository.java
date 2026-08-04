package com.sss.app.repository.integration.meta;

import com.sss.app.entity.integration.meta.MetaWebhookEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaWebhookEventRepository extends JpaRepository<MetaWebhookEvent, Long> {

    Page<MetaWebhookEvent> findAllByOrgIdAndPlatformOrderByReceivedAtDesc(Long orgId, String platform, Pageable pageable);
}
