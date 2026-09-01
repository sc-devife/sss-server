package com.sss.app.repository.itinerary;

import com.sss.app.entity.itinerary.ItineraryItemTransportLeg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryItemTransportLegRepository extends JpaRepository<ItineraryItemTransportLeg, Long> {

    List<ItineraryItemTransportLeg> findAllByItineraryItem_SeqpOrderByLegOrderAsc(Long itineraryItemSeqp);

    void deleteAllByItineraryItem_Seqp(Long itineraryItemSeqp);
}
