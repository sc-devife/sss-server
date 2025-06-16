package com.sss.app.repository;

import com.sss.app.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
  /*  Optional<UserSession> findByUsername(String username);

    Optional<UserSession> findByJwtToken(String jwtToken);

    void deleteByUsername(String username);

    void deleteByJwtToken(String jwtToken);

    Optional<UserSession> findByLastAccessedBefore(LocalDateTime dateTime);*/
}
