package com.sss.app.repository.itinerary;

import com.sss.app.entity.itinerary.ItineraryItemTransportDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItineraryItemTransportDetailRepository extends JpaRepository<ItineraryItemTransportDetail, Long> {

    Optional<ItineraryItemTransportDetail> findByItineraryItem_Seqp(Long itineraryItemSeqp);
}
