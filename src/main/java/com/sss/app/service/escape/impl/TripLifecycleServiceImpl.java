package com.sss.app.service.escape.impl;

import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.TripStatus;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.ConflictException;
import com.sss.app.helper.escape.EscapeHelper;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.escape.TripLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TripLifecycleServiceImpl implements TripLifecycleService {

    private static final String ENTITY_TYPE = "Escape";

    private final EscapeHelper escapeHelper;
    private final EscapeRepository escapeRepository;
    private final EscapeMapper escapeMapper;
    private final AuditLogService auditLogService;

    @Override
    public EscapeResponseDTO advance(Long tripId, String targetStatus) {
        Escape trip = escapeHelper.getEscapeById(tripId);

        int currentIndex = TripStatus.indexOf(trip.getStatus());
        int targetIndex = TripStatus.indexOf(targetStatus);

        if (targetIndex < 0) {
            throw new BadRequestException("Unknown trip status: " + targetStatus);
        }
        if (TripStatus.CANCELLED.equals(trip.getStatus())) {
            throw new ConflictException("This trip is cancelled and cannot move forward");
        }
        if (currentIndex < 0) {
            throw new ConflictException("Trip is in an unrecognized status: " + trip.getStatus());
        }
        if (targetIndex <= currentIndex) {
            throw new ConflictException("Cannot move from \"" + trip.getStatus() + "\" to \"" + targetStatus + "\" — status only moves forward");
        }

        String previousStatus = trip.getStatus();
        trip.setStatus(targetStatus);
        Escape saved = escapeRepository.save(trip);
        auditLogService.record(ENTITY_TYPE, tripId, "STATUS_ADVANCED", previousStatus, targetStatus);

        return escapeMapper.toResponse(saved);
    }

    @Override
    public EscapeResponseDTO cancel(Long tripId, String reason) {
        Escape trip = escapeHelper.getEscapeById(tripId);

        int currentIndex = TripStatus.indexOf(trip.getStatus());
        int ongoingIndex = TripStatus.indexOf(TripStatus.ONGOING);
        if (TripStatus.CANCELLED.equals(trip.getStatus())) {
            throw new ConflictException("This trip is already cancelled");
        }
        if (currentIndex >= ongoingIndex) {
            throw new ConflictException("Trips that are Ongoing or Completed can no longer be cancelled");
        }

        String previousStatus = trip.getStatus();
        trip.setStatus(TripStatus.CANCELLED);
        Escape saved = escapeRepository.save(trip);
        auditLogService.record(ENTITY_TYPE, tripId, "CANCELLED", previousStatus, reason);

        return escapeMapper.toResponse(saved);
    }
}
