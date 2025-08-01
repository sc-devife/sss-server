package com.sss.app.repository;

import com.sss.app.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUid(String uid);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.role
            WHERE u.uid = :uid
            """)
    Optional<User> findUserWithRoles(@Param("uid") String uid);

    boolean existsByEmail(String email);
}
