package com.sss.app.repository.quote;

import com.sss.app.entity.quote.QuoteLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteLineItemRepository extends JpaRepository<QuoteLineItem, Long> {

    Optional<QuoteLineItem> findByUid(UUID uid);

    long countByItineraryItem_SeqpIn(java.util.Collection<Long> itineraryItemSeqps);

    List<QuoteLineItem> findAllByQuote_SeqpOrderByDayNumberAscSortOrderAsc(Long quoteSeqp);

    Optional<QuoteLineItem> findByQuote_SeqpAndItineraryItem_Seqp(Long quoteSeqp, Long itineraryItemSeqp);
}
