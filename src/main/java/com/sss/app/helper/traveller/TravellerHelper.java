package com.sss.app.helper.traveller;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerUpdateRequestDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.mapper.traveller.TravellerMapper;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.traveller.TravellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TravellerHelper {
    private final TravellerRepository travellerRepository;
    private final TravellerMapper travellerMapper;

    public Traveller createTraveller(TravellerCreateRequestDTO payload) {

        if (travellerRepository.findByEmail(payload.getEmail()).isPresent()) {
            throw new ConflictException(
                    "Traveller with email '" + payload.getEmail() + "' already exists"
            );
        }
        Traveller traveller = travellerMapper.toEntityCreate(payload);
        return travellerRepository.save(traveller);
    }

    public Traveller updateTraveller(Long id, TravellerUpdateRequestDTO payload) {

        Traveller traveller = travellerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Traveller not found with id " + id));

        travellerMapper.updateEntityFromDto(payload, traveller);
        return travellerRepository.save(traveller);
       /// return travellerMapper.toResponse(travellerRepository.save(traveller));

       /* Traveller traveller = travellerMapper.updateEntityFromDto(payload);
        return travellerRepository.save(traveller);*/
    }

    public Traveller getTravellerById(Long seqp) {
        return travellerRepository.findById(seqp)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + seqp));
    }

   /* public List<Traveller> getAllLeadsByTripId() {
        return travellerRepository.findAll();
    }*/

}
