package com.sss.app.helper.traveller;

import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerUpdateRequestDTO;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.traveller.TravellerMapper;
import com.sss.app.repository.traveller.TravellerRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TravellerHelper {
    private final TravellerRepository travellerRepository;
    private final TravellerMapper travellerMapper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Traveller createTraveller(TravellerCreateRequestDTO payload) {

        if (travellerRepository.findByEmail(payload.getEmail()).isPresent()) {
            throw new ConflictException(
                    "Traveller with email '" + payload.getEmail() + "' already exists"
            );
        }
        Traveller traveller = travellerMapper.toEntityCreate(payload);
        traveller.setOrgId(currentUser().getOrgId());
        return travellerRepository.save(traveller);
    }

    public Traveller updateTraveller(Long id, TravellerUpdateRequestDTO payload) {
        Traveller traveller = getTravellerById(id);
        travellerMapper.updateEntityFromDto(payload, traveller);
        return travellerRepository.save(traveller);
    }

    public Traveller getTravellerById(Long seqp) {
        Traveller traveller = travellerRepository.findById(seqp)
                .orElseThrow(() -> new NotFoundException("Traveller not found with id: " + seqp));
        orgAccessGuard.requireAccessToOrg(traveller.getOrgId());
        return traveller;
    }

    public List<Traveller> getAllTravellers() {
        return travellerRepository.findAllByOrgId(currentUser().getOrgId());
    }

    public void deleteTraveller(Traveller traveller) {
        travellerRepository.delete(traveller);
    }
}
