package com.sss.app.repository;

import com.sss.app.entity.users.invitations.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvitationTokenRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByEmail(String email);

    Optional<UserInvitation> findByUid(String uid);

    @Query("""
            SELECT i FROM UserInvitation i
            WHERE i.orgId = :orgId AND i.is_used = false AND i.is_archived = false
            ORDER BY i.seqp DESC
            """)
    List<UserInvitation> findPendingByOrgId(@Param("orgId") Long orgId);
}
