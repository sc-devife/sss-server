package com.sss.app.helper.library.transport;

import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.serviceprovider.ServiceProvider;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.serviceprovider.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransportHelper {

    private final ServiceProviderRepository serviceProviderRepository;
    private final EscapePointRepository escapePointRepository;

    public ServiceProvider resolveProvider(UUID providerId) {
        return serviceProviderRepository.findByUid(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceProvider", providerId));
    }

    public EscapePoint resolveDestination(String destinationUid) {
        return escapePointRepository.findByUid(destinationUid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", destinationUid));
    }
}
