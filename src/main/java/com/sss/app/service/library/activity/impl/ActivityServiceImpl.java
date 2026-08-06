package com.sss.app.service.library.activity.impl;

import com.sss.app.dto.library.activity.ActivityCreateRequestDTO;
import com.sss.app.dto.library.activity.ActivityResponseDTO;
import com.sss.app.dto.library.activity.ActivityUpdateRequestDTO;
import com.sss.app.entity.library.activity.Activity;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.helper.library.activity.ActivityHelper;
import com.sss.app.mapper.library.activity.ActivityMapper;
import com.sss.app.repository.library.activity.ActivityRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.files.CloudinaryService;
import com.sss.app.service.library.activity.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final ActivityHelper activityHelper;
    private final OrgAccessGuard orgAccessGuard;
    private final CloudinaryService cloudinaryService;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public ActivityResponseDTO create(ActivityCreateRequestDTO dto) {
        Activity activity = activityMapper.toEntityCreate(dto);
        activity.setOrgId(currentUser().getOrgId());
        if (dto.getEscapePointId() != null) {
            activity.setEscapePoint(activityHelper.resolveEscapePoint(dto.getEscapePointId()));
        }
        Activity saved = activityRepository.save(activity);
        return activityMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponseDTO getById(UUID id) {
        return activityMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getAll() {
        return activityRepository.findAllByOrgIdAndDeletedAtIsNull(currentUser().getOrgId())
                .stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Override
    public ActivityResponseDTO update(UUID id, ActivityUpdateRequestDTO dto) {
        Activity activity = findEntityById(id);
        List<String> previousImages = activity.getImages() == null ? null : new ArrayList<>(activity.getImages());
        activityMapper.updateEntityFromDto(dto, activity);
        if (dto.getEscapePointId() != null) {
            activity.setEscapePoint(activityHelper.resolveEscapePoint(dto.getEscapePointId()));
        }
        Activity saved = activityRepository.save(activity);
        cloudinaryService.deleteRemoved(previousImages, saved.getImages());
        return activityMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Activity activity = findEntityById(id);
        activity.setDeletedAt(LocalDateTime.now());
        activity.setStatus("archived");
    }

    private Activity findEntityById(UUID id) {
        Activity activity = activityRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
        orgAccessGuard.requireAccessToOrg(activity.getOrgId());
        return activity;
    }
}
