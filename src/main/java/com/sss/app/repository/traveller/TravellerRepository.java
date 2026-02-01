package com.sss.app.repository.traveller;

import com.sss.app.entity.traveller.Traveller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravellerRepository extends JpaRepository<Traveller, Long> {
  //  Optional<Traveller> findByseqpAndIsDeletedFalse(Long seqp);

   // List<Traveller> findByIsDeletedFalse();

    Optional<Traveller> findByEmail(String email);

    //<T> ScopedValue<T> findBySeqp(Long seqp);
}
