package com.sss.app.repository.itinerary;

import com.sss.app.entity.itinerary.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, Long> {

    Optional<ItineraryItem> findByUid(UUID uid);

    List<ItineraryItem> findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(Long itinerarySeqp);

    @Query("SELECT ii FROM ItineraryItem ii " +
            "JOIN FETCH ii.itinerary it " +
            "JOIN FETCH it.escape e " +
            "LEFT JOIN FETCH e.lead " +
            "WHERE ii.itemType = 'hotel' AND ii.referenceId = :hotelUid " +
            "ORDER BY ii.createdAt DESC")
    List<ItineraryItem> findAllHotelBookings(@Param("hotelUid") UUID hotelUid);
}
