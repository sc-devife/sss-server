package com.sss.app.repository.itinerary;

import com.sss.app.entity.itinerary.ItineraryItemHotelInclusion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryItemHotelInclusionRepository extends JpaRepository<ItineraryItemHotelInclusion, Long> {

    List<ItineraryItemHotelInclusion> findAllByItineraryItem_SeqpOrderBySeqpAsc(Long itineraryItemSeqp);

    void deleteAllByItineraryItem_Seqp(Long itineraryItemSeqp);
}
