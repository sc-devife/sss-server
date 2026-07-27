package com.sss.app.repository.library.escapepoint;

import com.sss.app.entity.library.escapepoint.EscapePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface EscapePointRepository extends JpaRepository<EscapePoint, Long> {
    @Query("SELECT e FROM EscapePoint e WHERE e.orgId = :orgId AND e.deletedAt IS NULL")
    List<EscapePoint> findEscapePointsByOrgId(@Param("orgId") Long orgId);

    Optional<EscapePoint> findByUid(String uid);

    List<EscapePoint> findAllByUidIn(Set<String> uids);

    boolean existsById(String id);
}
