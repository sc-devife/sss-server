package com.sss.app.repository.library.hotel;

import com.sss.app.entity.library.hotel.Hotel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    Optional<Hotel> findByUid(UUID uid);

    List<Hotel> findAllByUidIn(List<UUID> uids);

    // location/escapePoint are the only *-to-one associations HotelMapper's
    // toResponse touches, so they're safe to fetch-join directly. The four
    // *-to-many (Set) associations are deliberately NOT joined here — fetch-
    // joining more than one collection at once multiplies the result set
    // (a cartesian product per combination across all four), which would
    // make a 500+ row list far worse, not better; Hotel.java's @BatchSize
    // on those fields covers them instead (batched IN-clause queries rather
    // than one query per hotel per collection).
    @EntityGraph(attributePaths = {"location", "escapePoint"})
    List<Hotel> findAllByOrgIdAndDeletedAtIsNull(Long orgId);

    // [escapePointSeqp, count] rows for the non-archived hotels linked to any of
    // the given Escape Points via the hotel's own escapePoint reference.
    @Query("select h.escapePoint.seqp, count(h) from Hotel h "
            + "where h.escapePoint.seqp in :escapePointSeqps and h.deletedAt is null "
            + "group by h.escapePoint.seqp")
    List<Object[]> countByEscapePointSeqps(@Param("escapePointSeqps") List<Long> escapePointSeqps);

    boolean existsByNameIgnoreCaseAndLocation_Uid(String name, UUID locationUid);
}
