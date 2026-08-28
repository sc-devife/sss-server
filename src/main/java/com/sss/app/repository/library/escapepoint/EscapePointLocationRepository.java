package com.sss.app.repository.library.escapepoint;

import com.sss.app.entity.library.escapepoint.EscapePointLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscapePointLocationRepository extends JpaRepository<EscapePointLocation, Long> {

    List<EscapePointLocation> findAllByEscapePoint_Seqp(Long escapePointSeqp);

    List<EscapePointLocation> findAllByEscapePoint_SeqpIn(List<Long> escapePointSeqps);
}
