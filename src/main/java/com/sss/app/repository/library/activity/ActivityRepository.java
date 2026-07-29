package com.sss.app.repository.library.activity;

import com.sss.app.entity.library.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Optional<Activity> findByUid(UUID uid);

    List<Activity> findAllByUidIn(List<UUID> uids);

    List<Activity> findAllByOrgIdAndDeletedAtIsNull(Long orgId);
}
