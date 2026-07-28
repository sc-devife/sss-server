package com.sss.app.repository.deal;

import com.sss.app.entity.deal.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, Long> {

    Optional<Deal> findByUid(UUID uid);

    Optional<Deal> findByEscape_Seqp(Long escapeSeqp);
}
