package com.sss.app.service.followup;

import com.sss.app.dto.followup.FollowUpCreateRequestDTO;
import com.sss.app.dto.followup.FollowUpResponseDTO;
import com.sss.app.dto.followup.FollowUpUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FollowUpService {
    FollowUpResponseDTO createFollowUp(FollowUpCreateRequestDTO request);
    FollowUpResponseDTO updateFollowUp(UUID uid, FollowUpUpdateRequestDTO request);
    FollowUpResponseDTO updateStatus(UUID uid, String status);
    Page<FollowUpResponseDTO> getAllForCurrentUser(String filter, String search, Pageable pageable);
    List<FollowUpResponseDTO> getAllForLead(UUID leadUid);
    List<FollowUpResponseDTO> getAllForEscape(UUID escapeUid);
    long countOpenForCurrentUser();
}
