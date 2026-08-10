package com.sss.app.repository.traveller;

import com.sss.app.entity.traveller.Traveller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TravellerRepository extends JpaRepository<Traveller, Long> {

    Optional<Traveller> findByEmail(String email);

    Optional<Traveller> findByUid(UUID uid);

    List<Traveller> findAllByUidIn(List<UUID> uids);

    List<Traveller> findAllByOrgId(Long orgId);
}
