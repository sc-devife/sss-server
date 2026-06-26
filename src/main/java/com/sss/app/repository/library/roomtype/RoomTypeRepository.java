package com.sss.app.repository.library.roomtype;

import com.sss.app.entity.library.roomtype.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    Optional<RoomType> findByUid(UUID uid);

    List<RoomType> findAllByUidIn(Set<UUID> uids);

    Optional<RoomType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
