package com.sss.app.repository.quote;

import com.sss.app.entity.quote.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Optional<Quote> findByUid(UUID uid);

    List<Quote> findAllByOrgIdAndItinerary_Seqp(Long orgId, Long itinerarySeqp);

    // Dashboard Quote Analytics — quotes/org is low-volume (one row per
    // itinerary version), so aggregating in Java is fine, consistent with
    // the existing revenuePipelineUsd calculation's style.
    List<Quote> findAllByOrgId(Long orgId);
}
