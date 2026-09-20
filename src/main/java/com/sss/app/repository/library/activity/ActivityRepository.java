package com.sss.app.repository.library.activity;

import com.sss.app.entity.library.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Optional<Activity> findByUid(UUID uid);

    List<Activity> findAllByUidIn(List<UUID> uids);

    List<Activity> findAllByOrgIdAndDeletedAtIsNull(Long orgId);

    // [escapePointSeqp, count] rows for the non-archived activities linked to
    // any of the given Escape Points.
    @Query("select a.escapePoint.seqp, count(a) from Activity a "
            + "where a.escapePoint.seqp in :escapePointSeqps and a.deletedAt is null "
            + "group by a.escapePoint.seqp")
    List<Object[]> countByEscapePointSeqps(@Param("escapePointSeqps") List<Long> escapePointSeqps);
}
