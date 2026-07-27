package com.sss.app.helper.library.activity;

import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityHelper {

    private final EscapePointRepository escapePointRepository;

    public EscapePoint resolveDestination(String destinationUid) {
        return escapePointRepository.findByUid(destinationUid)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", destinationUid));
    }
}
