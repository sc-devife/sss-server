package com.sss.app.repository.quotationtemplate;

import com.sss.app.entity.quotationtemplate.QuotationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuotationTemplateRepository extends JpaRepository<QuotationTemplate, Long> {

    Optional<QuotationTemplate> findByUid(UUID uid);

    List<QuotationTemplate> findAllByIsActiveTrue();
}
