package com.sss.app.repository;

import com.sss.app.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUid(String uid);

    Optional<User> findByEmail(String email);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.role
            WHERE u.uid = :uid
            """)
    Optional<User> findUserWithRoles(@Param("uid") String uid);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.role
            WHERE u.orgId = :orgId
            """)
    List<User> findUsersWithRoles(@Param("orgId") Long orgId);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.role
            WHERE u.email = :email
            """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByUserId(String userId);

    @Query("""
            SELECT u FROM User u
            WHERE u.email = :email OR u.contact_number = :mobileNumber
            ORDER BY u.createdAt DESC
            """)
    List<User> findByEmailOrContactNumberOrderByCreatedAtDesc(@Param("email") String email, @Param("mobileNumber") String mobileNumber);
}
