package com.sss.app.service.followup.impl;

import com.sss.app.dto.followup.FollowUpCreateRequestDTO;
import com.sss.app.dto.followup.FollowUpResponseDTO;
import com.sss.app.dto.followup.FollowUpUpdateRequestDTO;
import com.sss.app.entity.followup.FollowUp;
import com.sss.app.entity.users.User;
import com.sss.app.helper.followup.FollowUpsHelper;
import com.sss.app.repository.UserRepository;
import com.sss.app.service.followup.FollowUpEmailService;
import com.sss.app.service.followup.FollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpsHelper followUpsHelper;
    private final FollowUpEmailService followUpEmailService;
    private final UserRepository userRepository;

    @Override
    public FollowUpResponseDTO createFollowUp(FollowUpCreateRequestDTO request) {
        FollowUp saved = followUpsHelper.createFollowUp(request);
        // Synchronous, same as UserInvitationHelper's invitation email — kept
        // out of the create transaction's failure path by its own try/catch
        // inside FollowUpEmailService, so a mail-server hiccup never rolls
        // back (or blocks) the follow-up itself from being created.
        followUpEmailService.sendAssignmentEmail(saved);
        return toResponse(saved);
    }

    @Override
    public FollowUpResponseDTO updateFollowUp(UUID uid, FollowUpUpdateRequestDTO request) {
        return toResponse(followUpsHelper.updateFollowUp(uid, request));
    }

    @Override
    public FollowUpResponseDTO updateStatus(UUID uid, String status) {
        return toResponse(followUpsHelper.updateStatus(uid, status));
    }

    @Override
    public Page<FollowUpResponseDTO> getAllForCurrentUser(String filter, String search, Pageable pageable) {
        return followUpsHelper.getAllForCurrentUser(filter, search, pageable).map(this::toResponse);
    }

    @Override
    public List<FollowUpResponseDTO> getAllForLead(UUID leadUid) {
        return followUpsHelper.getAllForLead(leadUid).stream().map(this::toResponse).toList();
    }

    @Override
    public List<FollowUpResponseDTO> getAllForEscape(UUID escapeUid) {
        return followUpsHelper.getAllForEscape(escapeUid).stream().map(this::toResponse).toList();
    }

    @Override
    public long countOpenForCurrentUser() {
        return followUpsHelper.countOpenForCurrentUser();
    }

    private FollowUpResponseDTO toResponse(FollowUp entity) {
        FollowUpResponseDTO dto = new FollowUpResponseDTO();
        dto.setUid(entity.getUid());
        dto.setComment(entity.getComment());
        dto.setActionable(entity.getActionable());
        dto.setDueAt(entity.getDueAt());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCompletedAt(entity.getCompletedAt());

        if (entity.getLead() != null) {
            dto.setLeadUid(entity.getLead().getUid());
            dto.setLeadName(entity.getLead().getName());
        }
        if (entity.getEscape() != null) {
            dto.setEscapeUid(entity.getEscape().getUid());
            dto.setEscapeTripCode(entity.getEscape().getTripCode());
            if (entity.getEscape().getLead() != null) {
                dto.setLeadName(entity.getEscape().getLead().getName());
            }
        }
        if (entity.getAssignedTo() != null) {
            dto.setAssignedToUid(entity.getAssignedTo().getUid());
            dto.setAssignedToName(entity.getAssignedTo().getName());
        }
        if (entity.getCreatedBy() != null) {
            userRepository.findById(entity.getCreatedBy()).map(User::getName).ifPresent(dto::setCreatedByName);
        }
        return dto;
    }
}
