package com.sss.app.repository.library.hotel;

import com.sss.app.entity.library.hotel.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    Optional<Hotel> findByUid(UUID uid);

    List<Hotel> findAllByOrgIdAndDeletedAtIsNull(Long orgId);

    boolean existsByNameIgnoreCaseAndLocation_Uid(String name, UUID locationUid);
}
