package com.sss.app.service.traveller.impl;

import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerResponseDTO;
import com.sss.app.dto.traveller.TravellerUpdateRequestDTO;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.helper.traveller.TravellerHelper;
import com.sss.app.mapper.traveller.TravellerMapper;
import com.sss.app.repository.traveller.TravellerRepository;
import com.sss.app.service.traveller.TravellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TravellerServiceImpl implements TravellerService {

    private final TravellerMapper travellerMapper;
    private final TravellerHelper travellerHelper;

    @Override
    public TravellerResponseDTO createTraveller(TravellerCreateRequestDTO payload) {
        return travellerMapper.toResponse(travellerHelper.createTraveller(payload));
    }

    @Override
    public TravellerResponseDTO updateTraveller(Long id, TravellerUpdateRequestDTO payload) {
        return travellerMapper.toResponse(travellerHelper.updateTraveller(id, payload));
    }

    @Override
    public TravellerResponseDTO getTravellerById(Long id) {
        return travellerMapper.toResponse(travellerHelper.getTravellerById(id));
    }

    @Override
    public void deleteTraveller(Long id) {
        Traveller traveller = travellerHelper.getTravellerById(id); // org check
        travellerHelper.deleteTraveller(traveller);
    }

    @Override
    public List<TravellerResponseDTO> getAllTravellers() {
        return travellerHelper.getAllTravellers().stream().map(travellerMapper::toResponse).toList();
    }
}
