package com.sss.app.repository.library.transport;

import com.sss.app.entity.library.transport.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, Long> {

    Optional<Transport> findByUid(UUID uid);

    List<Transport> findAllByOrgIdAndDeletedAtIsNull(Long orgId);
}
