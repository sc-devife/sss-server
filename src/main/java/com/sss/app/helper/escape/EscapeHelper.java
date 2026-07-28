package com.sss.app.helper.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.TripStatus;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.escapesource.EscapeSource;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.escapesource.EscapeSourceRepository;
import com.sss.app.repository.traveller.TravellerRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Escape createEscape(EscapeCreateRequestDTO request) {

        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new NotFoundException("Lead not found"));
        orgAccessGuard.requireAccessToOrg(lead.getOrgId());

        EscapeSource source = request.getSourceId() == null ? null : sourceRepository.findById(request.getSourceId())
                .orElseThrow(() -> new NotFoundException("Source not found"));

        Escape trip = escapeMapper.toEntityCreate(request);

        trip.setOrgId(currentUser().getOrgId());
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

        trip.setStatus(TripStatus.PLANNING);

        return escapeRepository.save(trip);
    }

    public List<Escape> getAllEscapes() {
        return escapeRepository.findAllByOrgId(currentUser().getOrgId());
    }

    public Escape updateEscape(Long seqp, EscapeUpdateRequestDTO request) {
        //Fetch existing trip
        Escape escape = escapeRepository.findBySeqp(seqp)
                .orElseThrow(() -> new NotFoundException("Escape not found"));
        orgAccessGuard.requireAccessToOrg(escape.getOrgId());

        //Update Lead
        if (request.getLeadId() != null) {
            Lead lead = leadRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new NotFoundException("Lead not found"));
            orgAccessGuard.requireAccessToOrg(lead.getOrgId());
            escape.setLead(lead);
        }

        if (request.getSourceId() != null) {
            EscapeSource source = sourceRepository.findById(request.getSourceId())
                    .orElseThrow(() -> new NotFoundException("Source not found"));
            escape.setSource(source);
        }

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
        // Deliberately not setting status here — Section 8 requires status
        // changes to go through TripLifecycleService (validated + audited),
        // same "no freeform edits" pattern as Lead's lifecycle in Phase 3.
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
        Escape escape = escapeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip not found with id: " + id));
        orgAccessGuard.requireAccessToOrg(escape.getOrgId());
        return escape;
    }

    public void deleteEscape(Long seqp) {
        Escape escape = getEscapeById(seqp);
        escapeRepository.delete(escape);
    }
}
