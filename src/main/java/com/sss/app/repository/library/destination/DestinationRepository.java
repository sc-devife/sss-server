package com.sss.app.repository.library.destination;

import com.sss.app.entity.library.destination.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    Optional<Destination> findByUid(UUID uid);

    List<Destination> findAllByUidIn(Set<UUID> uids);

    Optional<Destination> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
