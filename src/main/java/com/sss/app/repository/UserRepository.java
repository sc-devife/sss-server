package com.sss.app.repository;

import com.sss.app.entity.users.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUid(String uid);

    // Row-locks the candidate during auto-assignment so two Leads created at
    // the same instant can't both read a stale "open workload" count and
    // both land on the same agent past their configured capacity — see
    // LeadAssignmentServiceImpl.selectAssignee. Must be called inside an
    // active transaction; the lock releases when that transaction commits.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.seqp = :seqp")
    Optional<User> lockForAssignment(@Param("seqp") Long seqp);

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

    // Same as findUsersWithRoles, newest-added user first — for the user
    // listings (/users/all). Kept separate so lead-assignment and notification
    // callers of findUsersWithRoles keep their existing (unordered) behavior.
    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.role
            WHERE u.orgId = :orgId
            ORDER BY u.createdAt DESC, u.seqp DESC
            """)
    List<User> findUsersWithRolesNewestFirst(@Param("orgId") Long orgId);

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
