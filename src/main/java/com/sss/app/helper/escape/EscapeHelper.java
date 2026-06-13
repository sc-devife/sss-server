package com.sss.app.helper.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.escapesource.EscapeSource;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.escapesource.EscapeSourceRepository;
import com.sss.app.repository.traveller.TravellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EscapeHelper {

    private final EscapeRepository escapeRepository;
    private final EscapeMapper escapeMapper;
    private final LeadRepository leadRepository;
    private final TravellerRepository travellerRepository;
    private final EscapePointRepository destinationRepository;
    private final EscapeSourceRepository sourceRepository;

    public Escape createEscape(EscapeCreateRequestDTO request) {

        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new NotFoundException("Lead not found"));

        EscapeSource source = sourceRepository.findById(request.getSourceId())
                .orElseThrow(() -> new NotFoundException("Source not found"));

        Escape trip = escapeMapper.toEntityCreate(request);

        trip.setLead(lead);
        trip.setSource(source);
        trip.setTravellers(
                new HashSet<>(travellerRepository.findAllById(request.getTravellerIds()))
        );

        trip.setDestinations(
                new HashSet<>(destinationRepository.findAllById(request.getDestinationIds()))
        );
        trip.setEndDate(
                request.getStartDate().plusDays(request.getNumberOfDays() - 1)
        );

        trip.setStatus("CREATED");

        return escapeRepository.save(trip);
    }
    public Escape updateEscape(Long seqp, EscapeUpdateRequestDTO request) {
        //Fetch existing trip
        Escape escape = escapeRepository.findBySeqp(seqp)
                .orElseThrow(() -> new RuntimeException("Escape not found"));

        //Update Lead
        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        EscapeSource source = sourceRepository.findById(request.getSourceId())
                .orElseThrow(() -> new NotFoundException("Source not found"));

        escape.setLead(lead);
        escape.setSource(source);

        //Update Travellers (Overwrite old ones)
        if (request.getTravellerIds() != null) {

            List<Traveller> travellers =
                    travellerRepository.findAllById(request.getTravellerIds());

            escape.getTravellers().clear(); // remove old
            escape.getTravellers().addAll(travellers); // add new
        }

        //Update Destinations (Overwrite old ones)
        if (request.getDestinationIds() != null) {

            List<EscapePoint> destinations =
                    destinationRepository.findAllById(request.getDestinationIds());

            escape.getDestinations().clear();
            escape.getDestinations().addAll(destinations);
        }

        //Update Other Fields
        escape.setStatus(request.getStatus());
        escape.setStartDate(request.getStartDate());
        escape.setNumberOfDays(request.getNumberOfDays());

        //Auto-calculate endDate
        if (request.getStartDate() != null && request.getNumberOfDays() != null) {
            escape.setEndDate(
                    request.getStartDate().plusDays(request.getNumberOfDays())
            );
        }

        //Save Updated Trip
        return escapeRepository.save(escape);
    }

    public Escape getEscapeById(Long id) {

        return escapeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + id));
    }
    public void deleteEscape(Long seqp) {

        Escape escape = escapeRepository.findById(seqp)
                .orElseThrow(() -> new NotFoundException("Trip not found with id = " + seqp));

        escapeRepository.delete(escape);
    }
}
