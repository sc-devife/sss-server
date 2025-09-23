package com.sss.app.repository;

import com.sss.app.entity.users.invitations.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationTokenRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByEmail(String email);
    Optional<UserInvitation> findByUid(String uid);
}
