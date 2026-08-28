package com.sss.app.repository.team;

import com.sss.app.entity.team.UserTeamLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTeamLinkRepository extends JpaRepository<UserTeamLink, Long> {

    List<UserTeamLink> findAllByUser_Seqp(Long userSeqp);

    List<UserTeamLink> findAllByUser_SeqpIn(List<Long> userSeqps);

    List<UserTeamLink> findAllByTeam_Seqp(Long teamSeqp);

    List<UserTeamLink> findAllByTeam_SeqpIn(List<Long> teamSeqps);
}
