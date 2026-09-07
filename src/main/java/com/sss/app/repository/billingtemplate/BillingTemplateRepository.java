package com.sss.app.repository.billingtemplate;

import com.sss.app.entity.billingtemplate.BillingTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingTemplateRepository extends JpaRepository<BillingTemplate, Long> {

    Optional<BillingTemplate> findByUid(UUID uid);

    List<BillingTemplate> findAllByIsActiveTrue();
}
