package com.sss.app.service.library.escapepoint.impl;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointLocationsUpdateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointPriorityImageRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.helper.library.escapepoint.EscapePointsHelper;
import com.sss.app.mapper.library.escapepoint.EscapePointMapper;
import com.sss.app.repository.library.activity.ActivityRepository;
import com.sss.app.repository.library.hotel.HotelRepository;
import com.sss.app.service.library.escapepoint.EscapePointLocationResolver;
import com.sss.app.service.library.escapepoint.EscapePointsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EscapePointsServiceImpl implements EscapePointsService {

    private final EscapePointsHelper escapePointsHelper;
    private final EscapePointMapper escapePointMapper;
    private final EscapePointLocationResolver escapePointLocationResolver;
    private final HotelRepository hotelRepository;
    private final ActivityRepository activityRepository;

    public EscapePointsServiceImpl(EscapePointsHelper escapePointsHelper, EscapePointMapper escapePointMapper,
                                    EscapePointLocationResolver escapePointLocationResolver,
                                    HotelRepository hotelRepository, ActivityRepository activityRepository) {
        this.escapePointsHelper = escapePointsHelper;
        this.escapePointMapper = escapePointMapper;
        this.escapePointLocationResolver = escapePointLocationResolver;
        this.hotelRepository = hotelRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    public List<EscapePointResponseDto> fetchAllEscapePoints() {
        List<EscapePoint> entities = escapePointsHelper.fetchAllEscapePoints();
        return enrich(entities, escapePointMapper.toDtoList(entities));
    }

    @Override
    public EscapePointResponseDto getEscapePointByUid(String uid) {
        EscapePoint entity = escapePointsHelper.getEscapePointByUid(uid);
        return enrich(List.of(entity), List.of(escapePointMapper.toDto(entity))).get(0);
    }

    @Override
    public EscapePointResponseDto createEscapePoint(EscapePointCreateRequestDto payload) {
        EscapePoint entity = escapePointsHelper.createEscapePoint(payload);
        return enrich(List.of(entity), List.of(escapePointMapper.toDto(entity))).get(0);
    }

    @Override
    public EscapePointResponseDto updateEscapePoint(String uid, EscapePointUpdateRequestDto payload) {
        EscapePoint entity = escapePointsHelper.updateEscapePoint(uid, payload);
        return enrich(List.of(entity), List.of(escapePointMapper.toDto(entity))).get(0);
    }

    @Override
    public void deleteEscapePoint(String uid) {
        escapePointsHelper.deleteEscapePoint(uid);
    }

    @Override
    public EscapePointResponseDto updateLocations(String uid, EscapePointLocationsUpdateRequestDto payload) {
        EscapePoint entity = escapePointsHelper.reassignLocations(uid, payload);
        return enrich(List.of(entity), List.of(escapePointMapper.toDto(entity))).get(0);
    }

    @Override
    public EscapePointResponseDto setPriorityImage(String uid, EscapePointPriorityImageRequestDto payload) {
        EscapePoint entity = escapePointsHelper.setPriorityImage(uid, payload.getImageUrl());
        return enrich(List.of(entity), List.of(escapePointMapper.toDto(entity))).get(0);
    }

    private List<EscapePointResponseDto> enrich(List<EscapePoint> entities, List<EscapePointResponseDto> dtos) {
        List<Long> seqps = entities.stream().map(EscapePoint::getSeqp).toList();
        escapePointLocationResolver.resolve(seqps, dtos);

        // Two grouped counts across every Escape Point in this response, rather
        // than one query per Escape Point.
        if (!seqps.isEmpty()) {
            Map<Long, Integer> hotels = toCountMap(hotelRepository.countByEscapePointSeqps(seqps));
            Map<Long, Integer> activities = toCountMap(activityRepository.countByEscapePointSeqps(seqps));
            for (int i = 0; i < seqps.size(); i++) {
                dtos.get(i).setHotelCount(hotels.getOrDefault(seqps.get(i), 0));
                dtos.get(i).setActivityCount(activities.getOrDefault(seqps.get(i), 0));
            }
        }
        return dtos;
    }

    private static Map<Long, Integer> toCountMap(List<Object[]> rows) {
        Map<Long, Integer> counts = new HashMap<>();
        for (Object[] row : rows) {
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }
        return counts;
    }
}
