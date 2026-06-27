package com.sss.app.service.library.mealplan.impl;

import com.sss.app.dto.library.mealplan.MealPlanCreateRequestDTO;
import com.sss.app.dto.library.mealplan.MealPlanResponseDTO;
import com.sss.app.dto.library.mealplan.MealPlanUpdateRequestDTO;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.mapper.library.mealplan.MealPlanMapper;
import com.sss.app.repository.library.mealplan.MealPlanRepository;
import com.sss.app.service.library.mealplan.MealPlanService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MealPlanServiceImpl implements MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanMapper mealPlanMapper;

    @Override
    public MealPlanResponseDTO create(MealPlanCreateRequestDTO dto) {
        if (mealPlanRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new EntityExistsException("MealPlan already exists with code: " + dto.getCode());
        }
        MealPlan mealPlan = mealPlanMapper.toEntityCreate(dto);
        MealPlan saved = mealPlanRepository.save(mealPlan);
        return mealPlanMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MealPlanResponseDTO getById(UUID id) {
        return mealPlanMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealPlanResponseDTO> getAll() {
        return mealPlanRepository.findAll()
                .stream()
                .map(mealPlanMapper::toResponse)
                .toList();
    }

    @Override
    public MealPlanResponseDTO update(UUID id, MealPlanUpdateRequestDTO dto) {
        MealPlan mealPlan = findEntityById(id);
        mealPlanMapper.updateEntityFromDto(dto, mealPlan);
        MealPlan saved = mealPlanRepository.save(mealPlan);
        return mealPlanMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        mealPlanRepository.delete(findEntityById(id));
    }

    private MealPlan findEntityById(UUID id) {
        return mealPlanRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("MealPlan", id));
    }
}
