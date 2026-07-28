package com.sss.app.repository.itinerary;

import com.sss.app.entity.itinerary.ItineraryContentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItineraryContentItemRepository extends JpaRepository<ItineraryContentItem, Long> {

    Optional<ItineraryContentItem> findByUid(UUID uid);

    List<ItineraryContentItem> findAllByItinerary_SeqpOrderByTypeAscSortOrderAsc(Long itinerarySeqp);
}
