package com.sss.app.helper.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeSummaryNotesRequestDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.organizations.OrganizationSettings;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.entity.users.User;
import com.sss.app.entity.notification.NotificationType;
import com.sss.app.dto.escape.DayReductionImpactDTO;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.itinerary.ItineraryItemHelper;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
import com.sss.app.repository.quote.QuoteLineItemRepository;
import org.springframework.beans.factory.ObjectProvider;
import com.sss.app.helper.traveller.TravellerHelper;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.OrganizationSettingsRepository;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.traveller.TravellerRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.assignment.LeadAssignmentService;
import com.sss.app.service.notification.NotificationService;
import com.sss.app.util.RichTextSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EscapeHelper {

    private final EscapeRepository escapeRepository;
    private final EscapeMapper escapeMapper;
    private final LeadRepository leadRepository;
    private final TravellerRepository travellerRepository;
    private final TravellerHelper travellerHelper;
    private final EscapePointRepository escapePointRepository;
    private final OrgAccessGuard orgAccessGuard;
    private final OrganizationSettingsRepository organizationSettingsRepository;
    private final LeadAssignmentService leadAssignmentService;
    private final NotificationService notificationService;
    private final ItineraryItemRepository itineraryItemRepository;
    private final QuoteLineItemRepository quoteLineItemRepository;
    // Provider: ItineraryItemHelper's dependency chain leads back to this helper (circular).
    private final ObjectProvider<ItineraryItemHelper> itineraryItemHelperProvider;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Escape createEscape(EscapeCreateRequestDTO request) {

        Lead lead = leadRepository.findByUid(request.getLeadUid())
                .orElseThrow(() -> new NotFoundException("Lead not found"));
        orgAccessGuard.requireAccessToOrg(lead.getOrgId());

        Escape trip = escapeMapper.toEntityCreate(request);

        trip.setOrgId(currentUser().getOrgId());
        trip.setLead(lead);
        trip.setTravellers(
                new HashSet<>(travellerRepository.findAllByUidIn(request.getTravellerUids()))
        );
        // First traveller submitted becomes the primary/lead contact — see
        // Escape.primaryTravellerUid for why this can't be inferred later.
        trip.setPrimaryTravellerUid(request.getTravellerUids().get(0));

        trip.setEscapePoints(
                new HashSet<>(escapePointRepository.findAllByUidIn(new HashSet<>(request.getEscapePointUids())))
        );
        trip.setEndDate(
                request.getStartDate().plusDays(request.getNumberOfDays() - 1)
        );

        trip.setStatus(EscapeStatus.PLANNING);

        Escape saved = escapeRepository.save(trip);

        notificationService.notifyUsers(
                notificationService.resolveOrgManagers(saved.getOrgId()), saved.getOrgId(),
                NotificationType.ESCAPE_CREATED, "Escape Created",
                "A new Escape has been created" + (lead != null ? " for " + lead.getName() : "") + ".",
                NotificationType.RelatedEntityType.ESCAPE, saved.getUid());

        // Conversion-time assignment — carries over the source Lead's own
        // assignment if it already has one (see LeadAssignmentServiceImpl),
        // otherwise scores fresh. Gated by the org's own setting, defaulting
        // to on if no settings row exists yet. Best-effort: a failure here
        // must not turn an otherwise-successful conversion into an error —
        // the Escape is simply left unassigned for manual pickup.
        boolean autoAssignEnabled = organizationSettingsRepository.findById(saved.getOrgId())
                .map(OrganizationSettings::getAutoAssignEnabled)
                .orElse(true);
        if (Boolean.TRUE.equals(autoAssignEnabled)) {
            try {
                leadAssignmentService.autoAssign(saved);
            } catch (Exception e) {
                log.error("Auto-assignment failed for Escape {} — left unassigned", saved.getUid(), e);
            }
        }

        return saved;
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
        // Shortening the escape removes the trailing days' planned items from
        // every itinerary (their quote lines go with them via the FK cascade).
        if (request.getNumberOfDays() != null) {
            if (request.getNumberOfDays() < 1) {
                throw new BadRequestException("An escape needs at least 1 day");
            }
            if (escape.getNumberOfDays() != null && request.getNumberOfDays() < escape.getNumberOfDays()) {
                for (ItineraryItem item : itineraryItemRepository
                        .findAllByItinerary_Escape_SeqpAndDayNumberGreaterThan(escape.getSeqp(), request.getNumberOfDays())) {
                    itineraryItemHelperProvider.getObject().delete(item.getUid());
                }
            }
        }

        escape.setStartDate(request.getStartDate());
        escape.setNumberOfDays(request.getNumberOfDays());

        //Auto-calculate endDate
        // numberOfDays counts inclusive day tabs (Day 1..Day N, Day 1 = the
        // start date — see ItineraryDayPlanner), so the last day's calendar
        // date is startDate + (numberOfDays - 1), matching createEscape's
        // formula above. This previously omitted the "-1" here, so an escape
        // whose duration was edited (the "Add Day" flow) ended up with an
        // endDate one day past its own last itinerary day.
        if (request.getStartDate() != null && request.getNumberOfDays() != null) {
            escape.setEndDate(
                    request.getStartDate().plusDays(request.getNumberOfDays() - 1)
            );
        }

        //Save Updated Escape
        return escapeRepository.save(escape);
    }

    /** What shortening the escape to {@code newNumberOfDays} days would delete — used to decide whether to warn first. */
    public DayReductionImpactDTO getDayReductionImpact(UUID uid, int newNumberOfDays) {
        Escape escape = getEscapeById(uid);
        List<ItineraryItem> doomed = itineraryItemRepository
                .findAllByItinerary_Escape_SeqpAndDayNumberGreaterThan(escape.getSeqp(), newNumberOfDays);
        List<Integer> days = doomed.stream().map(ItineraryItem::getDayNumber).distinct().sorted().toList();
        long quoteLines = doomed.isEmpty()
                ? 0
                : quoteLineItemRepository.countByItineraryItem_SeqpIn(doomed.stream().map(ItineraryItem::getSeqp).toList());
        return new DayReductionImpactDTO(days, doomed.size(), (int) quoteLines);
    }

    public Escape getEscapeById(UUID id) {
        Escape escape = escapeRepository.findByUid(id)
                .orElseThrow(() -> new NotFoundException("Escape not found with id: " + id));
        orgAccessGuard.requireAccessToOrg(escape.getOrgId());
        return escape;
    }

    // Its own small endpoint (Section 8's Summary tab) rather than riding
    // updateEscape's full-object PUT — that endpoint unconditionally
    // overwrites plain fields like startDate/numberOfDays on every save (see
    // above), and the duration-only save never carries these two fields,
    // which would silently null them out. internalComments is stored as-is
    // (plain text, never rendered as HTML); remarkForLead is rich text
    // rendered both here and in the Quotation, so it goes through the same
    // sanitizer as Terms/Inclusions/Exclusions content.
    public Escape updateSummaryNotes(UUID uid, EscapeSummaryNotesRequestDTO request) {
        Escape escape = getEscapeById(uid);
        escape.setInternalComments(request.getInternalComments());
        escape.setRemarkForLead(RichTextSanitizer.sanitize(request.getRemarkForLead()));
        return escapeRepository.save(escape);
    }

    // Adds a new traveller record to an already-created Escape — the
    // Travelers tab's "collect the 2nd/3rd traveller's details later" flow.
    // Reuses TravellerHelper.createTraveller (same org/duplicate-email
    // checks as the standalone /traveller/create endpoint) rather than
    // duplicating that logic; only appends to the existing set, unlike
    // updateEscape's travellerUids field which replaces it wholesale.
    public Escape addTraveller(UUID escapeUid, TravellerCreateRequestDTO request) {
        Escape escape = getEscapeById(escapeUid);
        Traveller traveller = travellerHelper.createTraveller(request);
        escape.getTravellers().add(traveller);
        return escapeRepository.save(escape);
    }

    // Detaches a traveller from this escape's roster only (removes the
    // escape_traveller join row) — does NOT delete the Traveller record
    // itself, since the same traveller may be linked to other escapes and a
    // hard delete would violate the escape_traveller FK constraint whenever
    // it's still referenced anywhere.
    public Escape removeTraveller(UUID escapeUid, UUID travellerUid) {
        Escape escape = getEscapeById(escapeUid);
        escape.getTravellers().removeIf(t -> t.getUid().equals(travellerUid));
        return escapeRepository.save(escape);
    }

    public void deleteEscape(UUID uid) {
        Escape escape = getEscapeById(uid);
        escapeRepository.delete(escape);
    }
}
