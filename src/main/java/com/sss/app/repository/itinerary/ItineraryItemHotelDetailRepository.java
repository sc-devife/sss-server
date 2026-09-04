package com.sss.app.repository.itinerary;

import com.sss.app.entity.itinerary.ItineraryItemHotelDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItineraryItemHotelDetailRepository extends JpaRepository<ItineraryItemHotelDetail, Long> {

    Optional<ItineraryItemHotelDetail> findByItineraryItem_Seqp(Long itineraryItemSeqp);

    // Sum of nights already committed to other hotel stays in the same
    // itinerary — the "excluding item" seqp lets an in-progress edit leave
    // its own current nights out of the total, so re-saving a hotel item
    // with an unchanged (or reduced) night count is never rejected against
    // its own prior value. A brand-new item's seqp simply never matches any
    // row here (it has no hotel detail yet), so the same call works for
    // create too.
    @Query("SELECT COALESCE(SUM(d.nights), 0) FROM ItineraryItemHotelDetail d " +
            "WHERE d.itineraryItem.itinerary.seqp = :itinerarySeqp AND d.itineraryItem.seqp <> :excludeItemSeqp")
    int sumNightsForItineraryExcludingItem(@Param("itinerarySeqp") Long itinerarySeqp, @Param("excludeItemSeqp") Long excludeItemSeqp);
}
