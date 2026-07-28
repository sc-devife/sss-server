package com.sss.app.repository.traveller;

import com.sss.app.entity.traveller.Traveller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravellerRepository extends JpaRepository<Traveller, Long> {

    Optional<Traveller> findByEmail(String email);

    List<Traveller> findAllByOrgId(Long orgId);
}
