package com.sss.app.repository.library.activity;

import com.sss.app.entity.library.activity.ActivityPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityPaymentRepository extends JpaRepository<ActivityPayment, Long> {

    Optional<ActivityPayment> findByUid(UUID uid);

    @Query("SELECT p FROM ActivityPayment p " +
            "JOIN FETCH p.escape e " +
            "WHERE p.activity.uid = :activityUid " +
            "ORDER BY p.paymentDate DESC, p.createdAt DESC")
    List<ActivityPayment> findAllByActivityUid(@Param("activityUid") UUID activityUid);

    // Backs Accounting > Transactions > Outgoing alongside HotelPayment.
    @Query("SELECT p FROM ActivityPayment p " +
            "JOIN FETCH p.activity a " +
            "JOIN FETCH p.escape e " +
            "WHERE p.orgId = :orgId " +
            "ORDER BY p.paymentDate DESC, p.createdAt DESC")
    List<ActivityPayment> findAllByOrgId(@Param("orgId") Long orgId);

    // Backs the Escape Payments workspace's supplier-payments section — see
    // HotelPaymentRepository.findAllByEscapeSeqp's own comment.
    @Query("SELECT p FROM ActivityPayment p " +
            "JOIN FETCH p.activity a " +
            "WHERE p.escape.seqp = :escapeSeqp " +
            "ORDER BY p.paymentDate DESC, p.createdAt DESC")
    List<ActivityPayment> findAllByEscapeSeqp(@Param("escapeSeqp") Long escapeSeqp);
}
