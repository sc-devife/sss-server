package com.sss.app.service.traveller;

import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerResponseDTO;
import com.sss.app.dto.traveller.TravellerUpdateRequestDTO;

import java.util.List;

public interface TravellerService {
    TravellerResponseDTO createTraveller(TravellerCreateRequestDTO request);
    TravellerResponseDTO updateTraveller(Long id, TravellerUpdateRequestDTO payload);
    TravellerResponseDTO getTravellerById(Long id);
    void deleteTraveller(Long id);
    List<TravellerResponseDTO> getAllTravellers();
}
