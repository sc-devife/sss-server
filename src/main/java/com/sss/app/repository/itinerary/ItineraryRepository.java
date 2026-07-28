package com.sss.app.repository.itinerary;

import com.sss.app.entity.itinerary.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    Optional<Itinerary> findByUid(UUID uid);

    List<Itinerary> findAllByOrgIdAndEscape_Seqp(Long orgId, Long escapeSeqp);

    List<Itinerary> findAllByOrgId(Long orgId);
}
