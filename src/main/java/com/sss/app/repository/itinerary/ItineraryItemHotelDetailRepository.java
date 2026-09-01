package com.sss.app.repository.itinerary;

import com.sss.app.entity.itinerary.ItineraryItemHotelDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItineraryItemHotelDetailRepository extends JpaRepository<ItineraryItemHotelDetail, Long> {

    Optional<ItineraryItemHotelDetail> findByItineraryItem_Seqp(Long itineraryItemSeqp);
}
