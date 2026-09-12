package com.sss.app.repository.library.amenity;

import com.sss.app.entity.library.amenity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    Optional<Amenity> findByUid(UUID uid);

    List<Amenity> findAllByIsActiveTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
