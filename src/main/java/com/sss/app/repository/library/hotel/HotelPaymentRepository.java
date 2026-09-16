package com.sss.app.repository.library.hotel;

import com.sss.app.entity.library.hotel.HotelPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HotelPaymentRepository extends JpaRepository<HotelPayment, Long> {

    Optional<HotelPayment> findByUid(UUID uid);

    @Query("SELECT p FROM HotelPayment p " +
            "JOIN FETCH p.escape e " +
            "WHERE p.hotel.uid = :hotelUid " +
            "ORDER BY p.paymentDate DESC, p.createdAt DESC")
    List<HotelPayment> findAllByHotelUid(@Param("hotelUid") UUID hotelUid);

    // Backs Accounting > Transactions > Outgoing — every hotel payout across
    // the whole org, not scoped to one hotel's own Payments tab.
    @Query("SELECT p FROM HotelPayment p " +
            "JOIN FETCH p.hotel h " +
            "JOIN FETCH p.escape e " +
            "WHERE p.orgId = :orgId " +
            "ORDER BY p.paymentDate DESC, p.createdAt DESC")
    List<HotelPayment> findAllByOrgId(@Param("orgId") Long orgId);
}
