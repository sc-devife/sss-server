package com.sss.app.repository.library.service;

import com.sss.app.entity.library.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    Optional<Service> findByUid(UUID uid);

    List<Service> findAllByUidIn(Set<UUID> uids);

    // Global master data only — what the main Services module shows/manages.
    List<Service> findAllByHotelIsNull();

    boolean existsByNameIgnoreCaseAndHotelIsNull(String name);

    // What one hotel's Add/Edit form should offer: every global service,
    // plus any service scoped specifically to this hotel. The LEFT JOIN is
    // required — a plain `s.hotel.uid` path expression compiles to an
    // implicit INNER join hoisted into the FROM clause, which would drop
    // every global (hotel IS NULL) row before the WHERE clause ever runs.
    @Query("SELECT s FROM Service s LEFT JOIN s.hotel h WHERE s.hotel IS NULL OR h.uid = :hotelUid")
    List<Service> findAllVisibleToHotel(@Param("hotelUid") UUID hotelUid);

    @Query("SELECT COUNT(s) > 0 FROM Service s LEFT JOIN s.hotel h WHERE LOWER(s.name) = LOWER(:name) AND (s.hotel IS NULL OR h.uid = :hotelUid)")
    boolean existsByNameIgnoreCaseVisibleToHotel(@Param("name") String name, @Param("hotelUid") UUID hotelUid);
}
