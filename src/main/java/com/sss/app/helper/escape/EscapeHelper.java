package com.sss.app.helper.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EscapeHelper {

    private final EscapeRepository escapeRepository;
    private final EscapeMapper escapeMapper;
    private final LeadRepository leadRepository;
    private final TravellerRepository travellerRepository;
    private final EscapePointRepository escapePointRepository;
    private final EscapeSourceRepository sourceRepository;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Escape createEscape(EscapeCreateRequestDTO request) {

        Lead lead = leadRepository.findByUid(request.getLeadUid())
                .orElseThrow(() -> new NotFoundException("Lead not found"));
        orgAccessGuard.requireAccessToOrg(lead.getOrgId());

        EscapeSource source = request.getSourceUid() == null ? null : sourceRepository.findByUid(request.getSourceUid())
                .orElseThrow(() -> new NotFoundException("Source not found"));

        Escape trip = escapeMapper.toEntityCreate(request);

        trip.setOrgId(currentUser().getOrgId());
        trip.setLead(lead);
        trip.setSource(source);
        trip.setTravellers(
                new HashSet<>(travellerRepository.findAllByUidIn(request.getTravellerUids()))
        );

        trip.setEscapePoints(
                new HashSet<>(escapePointRepository.findAllByUidIn(new HashSet<>(request.getEscapePointUids())))
        );
        trip.setEndDate(
                request.getStartDate().plusDays(request.getNumberOfDays() - 1)
        );

        trip.setStatus(EscapeStatus.PLANNING);

        return escapeRepository.save(trip);
    }

    public List<Escape> getAllEscapes() {
        return escapeRepository.findAllByOrgId(currentUser().getOrgId());
    }

    public Escape updateEscape(UUID uid, EscapeUpdateRequestDTO request) {
        //Fetch existing trip
        Escape escape = escapeRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Escape not found"));
        orgAccessGuard.requireAccessToOrg(escape.getOrgId());

        //Update Lead
        if (request.getLeadUid() != null) {
            Lead lead = leadRepository.findByUid(request.getLeadUid())
                    .orElseThrow(() -> new NotFoundException("Lead not found"));
            orgAccessGuard.requireAccessToOrg(lead.getOrgId());
            escape.setLead(lead);
        }

        if (request.getSourceUid() != null) {
            EscapeSource source = sourceRepository.findByUid(request.getSourceUid())
                    .orElseThrow(() -> new NotFoundException("Source not found"));
            escape.setSource(source);
        }

        //Update Travellers (Overwrite old ones)
        if (request.getTravellerUids() != null) {

            List<Traveller> travellers =
                    travellerRepository.findAllByUidIn(request.getTravellerUids());

            escape.getTravellers().clear(); // remove old
            escape.getTravellers().addAll(travellers); // add new
        }

        //Update Escape Points (Overwrite old ones)
        if (request.getEscapePointUids() != null) {

            List<EscapePoint> escapePoints =
                    escapePointRepository.findAllByUidIn(new HashSet<>(request.getEscapePointUids()));

            escape.getEscapePoints().clear();
            escape.getEscapePoints().addAll(escapePoints);
        }

        //Update Other Fields
        // Deliberately not setting status here — Section 8 requires status
        // changes to go through EscapeLifecycleService (validated + audited),
        // same "no freeform edits" pattern as Lead's lifecycle in Phase 3.
        escape.setStartDate(request.getStartDate());
        escape.setNumberOfDays(request.getNumberOfDays());

        //Auto-calculate endDate
        if (request.getStartDate() != null && request.getNumberOfDays() != null) {
            escape.setEndDate(
                    request.getStartDate().plusDays(request.getNumberOfDays())
            );
        }

        //Save Updated Escape
        return escapeRepository.save(escape);
    }

    public Escape getEscapeById(UUID id) {
        Escape escape = escapeRepository.findByUid(id)
                .orElseThrow(() -> new NotFoundException("Escape not found with id: " + id));
        orgAccessGuard.requireAccessToOrg(escape.getOrgId());
        return escape;
    }

    public void deleteEscape(UUID uid) {
        Escape escape = getEscapeById(uid);
        escapeRepository.delete(escape);
    }
}
