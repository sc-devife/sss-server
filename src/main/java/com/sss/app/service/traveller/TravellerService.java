package com.sss.app.service.traveller;

import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerResponseDTO;
import com.sss.app.dto.traveller.TravellerUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface TravellerService {
    TravellerResponseDTO createTraveller(TravellerCreateRequestDTO request);
    TravellerResponseDTO updateTraveller(UUID id, TravellerUpdateRequestDTO payload);
    TravellerResponseDTO getTravellerById(UUID id);
    void deleteTraveller(UUID id);
    List<TravellerResponseDTO> getAllTravellers();
}
