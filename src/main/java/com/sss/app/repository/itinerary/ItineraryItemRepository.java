package com.sss.app.repository.itinerary;

import com.sss.app.entity.itinerary.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, Long> {

    Optional<ItineraryItem> findByUid(UUID uid);

    List<ItineraryItem> findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(Long itinerarySeqp);
}
