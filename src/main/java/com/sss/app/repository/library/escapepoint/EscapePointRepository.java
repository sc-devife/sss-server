package com.sss.app.repository.library.escapepoint;

import com.sss.app.entity.library.escapepoint.EscapePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface EscapePointRepository extends JpaRepository<EscapePoint, Long> {
    @Query("SELECT e FROM EscapePoint e WHERE e.company_id = :companyId")
    List<EscapePoint> findEscapePointsByCompanyId(@Param("companyId") Long companyId);

    Optional<EscapePoint> findByUid(String uid);

    boolean existsById(String id);
}
