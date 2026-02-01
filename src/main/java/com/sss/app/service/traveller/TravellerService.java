package com.sss.app.service.traveller;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerResponseDTO;
import com.sss.app.dto.traveller.TravellerUpdateRequestDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface TravellerService {
    TravellerResponseDTO createTraveller(TravellerCreateRequestDTO request);
    TravellerResponseDTO updateTraveller(Long id, TravellerUpdateRequestDTO payload);
    TravellerResponseDTO getTravellerById(Long id);
    void deleteTraveller(Long id);
 //   List<TravellerResponseDTO> getAllTravellerByTripId();
}
