package com.sss.app.service.escape.impl;

import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.ConflictException;
import com.sss.app.helper.escape.EscapeHelper;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.escape.EscapeLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EscapeLifecycleServiceImpl implements EscapeLifecycleService {

    private static final String ENTITY_TYPE = "Escape";

    private final EscapeHelper escapeHelper;
    private final EscapeRepository escapeRepository;
    private final EscapeMapper escapeMapper;
    private final AuditLogService auditLogService;

    @Override
    public EscapeResponseDTO advance(Long escapeId, String targetStatus) {
        Escape escape = escapeHelper.getEscapeById(escapeId);

        int currentIndex = EscapeStatus.indexOf(escape.getStatus());
        int targetIndex = EscapeStatus.indexOf(targetStatus);

        if (targetIndex < 0) {
            throw new BadRequestException("Unknown escape status: " + targetStatus);
        }
        if (EscapeStatus.CANCELLED.equals(escape.getStatus())) {
            throw new ConflictException("This escape is cancelled and cannot move forward");
        }
        if (currentIndex < 0) {
            throw new ConflictException("Escape is in an unrecognized status: " + escape.getStatus());
        }
        if (targetIndex <= currentIndex) {
            throw new ConflictException("Cannot move from \"" + escape.getStatus() + "\" to \"" + targetStatus + "\" — status only moves forward");
        }

        String previousStatus = escape.getStatus();
        escape.setStatus(targetStatus);
        Escape saved = escapeRepository.save(escape);
        auditLogService.record(ENTITY_TYPE, escapeId, "STATUS_ADVANCED", previousStatus, targetStatus);

        return escapeMapper.toResponse(saved);
    }

    @Override
    public EscapeResponseDTO cancel(Long escapeId, String reason) {
        Escape escape = escapeHelper.getEscapeById(escapeId);

        int currentIndex = EscapeStatus.indexOf(escape.getStatus());
        int ongoingIndex = EscapeStatus.indexOf(EscapeStatus.ONGOING);
        if (EscapeStatus.CANCELLED.equals(escape.getStatus())) {
            throw new ConflictException("This escape is already cancelled");
        }
        if (currentIndex >= ongoingIndex) {
            throw new ConflictException("Escapes that are Ongoing or Completed can no longer be cancelled");
        }

        String previousStatus = escape.getStatus();
        escape.setStatus(EscapeStatus.CANCELLED);
        Escape saved = escapeRepository.save(escape);
        auditLogService.record(ENTITY_TYPE, escapeId, "CANCELLED", previousStatus, reason);

        return escapeMapper.toResponse(saved);
    }
}
