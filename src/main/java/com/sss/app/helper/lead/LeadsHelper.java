package com.sss.app.helper.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.entity.integration.meta.LeadSourceMetadata;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.repository.integration.meta.LeadSourceMetadataRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.integration.ProviderLeadMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeadsHelper {

    private static final int NOTES_MAX_LENGTH = 500;

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;
    private final OrgAccessGuard orgAccessGuard;
    private final EscapePointRepository escapePointRepository;
    private final LeadSourceMetadataRepository leadSourceMetadataRepository;

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
        if (payload.getEscapePointId() != null && !payload.getEscapePointId().isBlank()) {
            escapePointRepository.findByUid(payload.getEscapePointId())
                    .ifPresent(lead::setEscapePointRef);
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

    public record ChannelLeadResult(Lead lead, boolean wasDuplicate) {}

    /**
     * Lead Source Integration (Meta) path: same channel-intake contract as
     * the method above, but with duplicate detection (by email or phone,
     * scoped to org) and provider metadata persistence. Deliberately a
     * separate method rather than a change to createLeadFromChannel(...)
     * above, which the plain "webhook" channel still uses unmodified.
     */
    public ChannelLeadResult createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload,
                                                    ProviderLeadMetadata sourceMetadata) {
        Optional<Lead> duplicate = findDuplicate(orgId, payload.getEmail(), payload.getPhone());
        if (duplicate.isPresent()) {
            Lead existing = duplicate.get();
            appendDuplicateNote(existing, channelCode, payload.getSourceRefId());
            leadRepository.save(existing);
            return new ChannelLeadResult(existing, true);
        }

        Lead lead = Lead.builder()
                .orgId(orgId)
                .name(payload.getName())
                .email(payload.getEmail())
                .phone(payload.getPhone())
                .destination(payload.getDestinationHint())
                .travelDate(payload.getTravelDate())
                .numberOfPeople(payload.getNumberOfPeople())
                .durationDays(payload.getDurationDays())
                .status("New")
                .sourceCode(channelCode)
                .sourceRefId(payload.getSourceRefId())
                .notes(truncateNotes(payload.getNotes()))
                .build();
        lead = leadRepository.save(lead);

        if (sourceMetadata != null) {
            LeadSourceMetadata metadata = LeadSourceMetadata.builder()
                    .leadId(lead.getSeqp())
                    .orgId(orgId)
                    .provider(sourceMetadata.provider())
                    .platformLeadId(sourceMetadata.platformLeadId())
                    .campaignId(sourceMetadata.campaignId())
                    .campaignName(sourceMetadata.campaignName())
                    .adsetId(sourceMetadata.adsetId())
                    .adId(sourceMetadata.adId())
                    .adName(sourceMetadata.adName())
                    .formId(sourceMetadata.formId())
                    .formName(sourceMetadata.formName())
                    .pageId(sourceMetadata.pageId())
                    .receivedAt(sourceMetadata.receivedAt())
                    .rawFieldData(sourceMetadata.rawFieldData())
                    .build();
            leadSourceMetadataRepository.save(metadata);
        }

        return new ChannelLeadResult(lead, false);
    }

    private Optional<Lead> findDuplicate(Long orgId, String email, String phone) {
        if (email != null && !email.isBlank()) {
            Optional<Lead> byEmail = leadRepository.findFirstByOrgIdAndEmailIgnoreCase(orgId, email);
            if (byEmail.isPresent()) {
                return byEmail;
            }
        }
        if (phone != null && !phone.isBlank()) {
            return leadRepository.findFirstByOrgIdAndPhone(orgId, phone);
        }
        return Optional.empty();
    }

    private void appendDuplicateNote(Lead lead, String channelCode, String sourceRefId) {
        String label = "facebook".equals(channelCode) ? "Facebook Lead Ads"
                : "instagram".equals(channelCode) ? "Instagram Lead Ads"
                : channelCode;
        String note = "[%s] Re-submitted lead form on %s (leadgen_id=%s) — merged as duplicate, no new record created."
                .formatted(label, LocalDate.now(), sourceRefId);
        String existing = lead.getNotes();
        String combined = (existing == null || existing.isBlank()) ? note : existing + "\n" + note;
        lead.setNotes(truncateNotes(combined));
    }

    private String truncateNotes(String notes) {
        if (notes == null) return null;
        return notes.length() > NOTES_MAX_LENGTH ? notes.substring(notes.length() - NOTES_MAX_LENGTH) : notes;
    }

    public Lead getLeadById(UUID id) {
        Lead lead = leadRepository.findByUid(id)
                .orElseThrow(() -> new NotFoundException("Lead not found with id: " + id));
        orgAccessGuard.requireAccessToOrg(lead.getOrgId());
        return lead;
    }

    public List<Lead> getAllLeads() {
        return leadRepository.findAllByOrgId(currentUser().getOrgId());
    }

    // Deliberately not a lifecycle action (no status implication) — a plain
    // field setter, same shape as any other lead detail, so it doesn't need
    // the audit-logged ceremony the status-changing actions go through.
    public Lead setFollowUpDueDate(UUID id, LocalDate followUpDueDate) {
        Lead lead = getLeadById(id);
        lead.setFollowUpDueDate(followUpDueDate);
        return leadRepository.save(lead);
    }

}
