package com.sss.app.repository.library.transport;

import com.sss.app.entity.library.transport.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, Long> {

    Optional<Transport> findByUid(UUID uid);

    List<Transport> findAllByUidIn(List<UUID> uids);

    List<Transport> findAllByOrgIdAndDeletedAtIsNull(Long orgId);

    // Backs the Service Provider Detail page's "Vehicles" section — every
    // vehicle linked to this provider (Multi Vehicle Owner transports only;
    // Single Vehicle Owner ones never set provider — see TransportPanel).
    List<Transport> findAllByProvider_UidAndOrgIdAndDeletedAtIsNull(UUID providerUid, Long orgId);
}
