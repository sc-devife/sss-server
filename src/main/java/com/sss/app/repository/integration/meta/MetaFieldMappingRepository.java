package com.sss.app.repository.integration.meta;

import com.sss.app.entity.integration.meta.MetaFieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaFieldMappingRepository extends JpaRepository<MetaFieldMapping, Long> {

    List<MetaFieldMapping> findAllByOrgIdAndFormIdIsNull(Long orgId);

    List<MetaFieldMapping> findAllByOrgIdAndFormId(Long orgId, String formId);

    List<MetaFieldMapping> findAllByOrgId(Long orgId);
}
