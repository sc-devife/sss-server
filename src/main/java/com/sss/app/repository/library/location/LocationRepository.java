package com.sss.app.repository.library.location;

import com.sss.app.entity.library.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByUid(UUID uid);

    Optional<Location> findByDisplayNameIgnoreCase(String displayName);

    boolean existsByDisplayNameIgnoreCase(String displayName);
}
