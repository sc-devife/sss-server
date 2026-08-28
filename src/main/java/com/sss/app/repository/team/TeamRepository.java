package com.sss.app.repository.team;

import com.sss.app.entity.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByUid(UUID uid);

    List<Team> findAllByUidIn(List<UUID> uids);

    List<Team> findAllByOrgId(Long orgId);

    List<Team> findAllByOrgIdAndStatus(Long orgId, String status);
}
