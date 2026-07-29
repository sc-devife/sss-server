package com.sss.app.helper.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LeadsHelper {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;
    private final OrgAccessGuard orgAccessGuard;
    private final EscapePointRepository escapePointRepository;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Lead createLead(LeadCreateRequestDTO payload) {
        Lead lead = leadMapper.toEntity(payload);
        lead.setOrgId(currentUser().getOrgId());
        lead.setStatus("New");
        if (lead.getIsPriority() == null) {
            lead.setIsPriority(false);
        }
        if (lead.getSourceCode() == null || lead.getSourceCode().isBlank()) {
            lead.setSourceCode("manual");
        }
        if (payload.getDestinationId() != null && !payload.getDestinationId().isBlank()) {
            escapePointRepository.findByUid(payload.getDestinationId())
                    .ifPresent(lead::setDestinationRef);
        }
        return leadRepository.save(lead);
    }

    /**
     * Channel-intake path (Section 7): no authenticated principal exists for
     * an inbound webhook call, so orgId is passed explicitly rather than
     * resolved from currentUser().
     */
    public Lead createLeadFromChannel(Long orgId, String channelCode, String sourceRefId, String name,
                                       String email, String phone, String destinationHint,
                                       java.time.LocalDate travelDate, Integer numberOfPeople, Integer durationDays) {
        Lead lead = Lead.builder()
                .orgId(orgId)
                .name(name)
                .email(email)
                .phone(phone)
                .destination(destinationHint)
                .travelDate(travelDate)
                .numberOfPeople(numberOfPeople)
                .durationDays(durationDays)
                .status("New")
                .sourceCode(channelCode)
                .sourceRefId(sourceRefId)
                .build();
        return leadRepository.save(lead);
    }

    public Lead getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lead not found with id: " + id));
        orgAccessGuard.requireAccessToOrg(lead.getOrgId());
        return lead;
    }

    public List<Lead> getAllLeads() {
        return leadRepository.findAllByOrgId(currentUser().getOrgId());
    }

}
