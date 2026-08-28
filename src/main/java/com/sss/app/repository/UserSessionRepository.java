package com.sss.app.repository;

import com.sss.app.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findAllByUser_SeqpAndRevokedAtIsNullOrderByLastAccessedDesc(Long userSeqp);

    List<UserSession> findAllByUser_SeqpInAndRevokedAtIsNull(List<Long> userSeqps);

    // @Transactional directly on these two: both are called from places with
    // no ambient transaction of their own (JwtAuthenticationFilter's
    // doFilter, UserSessionsController's endpoints), and a @Modifying query
    // throws ("Executing an update/delete query") without one.
    @Transactional
    @Modifying
    @Query("UPDATE UserSession s SET s.revokedAt = :now WHERE s.user.seqp = :userSeqp AND s.revokedAt IS NULL")
    void revokeAllActiveForUser(@Param("userSeqp") Long userSeqp, @Param("now") LocalDateTime now);

    @Transactional
    @Modifying
    @Query("UPDATE UserSession s SET s.revokedAt = :now WHERE s.user.seqp = :userSeqp AND s.revokedAt IS NULL AND s.sessionId <> :exceptSessionId")
    void revokeAllActiveForUserExcept(@Param("userSeqp") Long userSeqp, @Param("exceptSessionId") UUID exceptSessionId, @Param("now") LocalDateTime now);
}
