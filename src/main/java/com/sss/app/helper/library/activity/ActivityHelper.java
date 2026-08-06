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

    public EscapePoint resolveEscapePoint(String escapePointUid) {
        return escapePointRepository.findByUid(escapePointUid)
                .orElseThrow(() -> new ResourceNotFoundException("EscapePoint", escapePointUid));
    }
}
