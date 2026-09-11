package com.sss.app.helper.itinerary;

import com.sss.app.dto.itinerary.HotelDetailDTO;
import com.sss.app.dto.itinerary.HotelInclusionDTO;
import com.sss.app.dto.itinerary.ItineraryItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReorderRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemUpdateRequestDTO;
import com.sss.app.dto.itinerary.TransportDetailDTO;
import com.sss.app.dto.itinerary.TransportLegDTO;
import com.sss.app.dto.quote.QuoteComputeRequestDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.itinerary.ItineraryItemHotelDetail;
import com.sss.app.entity.itinerary.ItineraryItemHotelInclusion;
import com.sss.app.entity.itinerary.ItineraryItemTransportDetail;
import com.sss.app.entity.itinerary.ItineraryItemTransportLeg;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.roomtype.RoomType;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.itinerary.ItineraryItemHotelDetailRepository;
import com.sss.app.repository.itinerary.ItineraryItemHotelInclusionRepository;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
import com.sss.app.repository.itinerary.ItineraryItemTransportDetailRepository;
import com.sss.app.repository.itinerary.ItineraryItemTransportLegRepository;
import com.sss.app.repository.library.activity.ActivityRepository;
import com.sss.app.repository.library.hotel.HotelRepository;
import com.sss.app.repository.library.mealplan.MealPlanRepository;
import com.sss.app.repository.library.roomtype.RoomTypeRepository;
import com.sss.app.repository.library.serviceprovider.ServiceProviderRepository;
import com.sss.app.repository.library.transport.TransportRepository;
import com.sss.app.repository.quote.QuoteRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.quote.QuoteComputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class ItineraryItemHelper {

    private static final Set<String> VALID_TYPES = Set.of(
            "transport", "pickup_drop", "hotel", "activity", "sightseeing", "meal", "free_time", "other");

    // Which library table (if any) referenceId points into, per itemType.
    // transport/pickup_drop share Transport, activity/sightseeing share
    // Activity, meal/other point at ServiceProvider (restaurant/guide/misc
    // vendor); free_time never carries a reference.
    private enum RefKind { HOTEL, ACTIVITY, TRANSPORT, SERVICE_PROVIDER, NONE }

    private static final Map<String, RefKind> REF_KIND_BY_TYPE = Map.of(
            "transport", RefKind.TRANSPORT,
            "pickup_drop", RefKind.TRANSPORT,
            "hotel", RefKind.HOTEL,
            "activity", RefKind.ACTIVITY,
            "sightseeing", RefKind.ACTIVITY,
            "meal", RefKind.SERVICE_PROVIDER,
            "other", RefKind.SERVICE_PROVIDER,
            "free_time", RefKind.NONE);

    private final ItineraryItemRepository itineraryItemRepository;
    private final ItineraryHelper itineraryHelper;
    private final HotelRepository hotelRepository;
    private final ActivityRepository activityRepository;
    private final TransportRepository transportRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ItineraryItemTransportDetailRepository transportDetailRepository;
    private final ItineraryItemTransportLegRepository transportLegRepository;
    private final ItineraryItemHotelDetailRepository hotelDetailRepository;
    private final ItineraryItemHotelInclusionRepository hotelInclusionRepository;
    private final MealPlanRepository mealPlanRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final OrgAccessGuard orgAccessGuard;
    private final AuditLogService auditLogService;
    private final QuoteRepository quoteRepository;
    private final QuoteComputationService quoteComputationService;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public ItineraryItem create(ItineraryItemCreateRequestDTO request) {
        Itinerary itinerary = itineraryHelper.getByUid(request.getItineraryUid());
        validateReference(request.getItemType(), request.getReferenceId(), request.getTitle());

        ItineraryItem item = ItineraryItem.builder()
                .orgId(currentUser().getOrgId())
                .itinerary(itinerary)
                .dayNumber(request.getDayNumber())
                .itemType(request.getItemType())
                .referenceId(request.getReferenceId())
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .notes(request.getNotes())
                .longDescription(request.getLongDescription())
                .price(request.getPrice())
                .sortOrder(nextSortOrder(itinerary.getSeqp()))
                .build();

        ItineraryItem saved = itineraryItemRepository.save(item);
        if (request.getTransportDetail() != null) {
            saveTransportDetail(saved, request.getTransportDetail());
        }
        if (request.getHotelDetail() != null) {
            saveHotelDetail(saved, request.getHotelDetail());
        }
        return saved;
    }

    public List<ItineraryItem> getAllForItinerary(UUID itineraryUid) {
        Itinerary itinerary = itineraryHelper.getByUid(itineraryUid);
        return itineraryItemRepository.findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(itinerary.getSeqp());
    }

    public ItineraryItem update(UUID uid, ItineraryItemUpdateRequestDTO request) {
        ItineraryItem item = getByUid(uid);
        String previousStatus = item.getStatus();

        // Item-level Drop (currently Activity only — Hotel has its own
        // separate Drop path on HotelDetailDTO/saveHotelDetail). Mirrors
        // that same rule: only status/droppingReason/cancellationChargeInr
        // are written, every other field (title, price, notes, etc.) is
        // left exactly as it was.
        boolean droppingItemNow = "activity".equals(item.getItemType())
                && BookingStatus.DROP.equals(request.getStatus())
                && !BookingStatus.DROP.equals(previousStatus);

        if (droppingItemNow) {
            if (request.getDroppingReason() == null || request.getDroppingReason().isBlank()) {
                throw new BadRequestException("Dropping reason is required to mark an activity as Drop");
            }
            item.setStatus(BookingStatus.DROP);
            item.setDroppingReason(request.getDroppingReason());
            item.setCancellationChargeInr(request.getCancellationCharge());
        } else {
            if (request.getItemType() != null || request.getReferenceId() != null || request.getTitle() != null) {
                String newType = request.getItemType() != null ? request.getItemType() : item.getItemType();
                UUID newRefId = request.getReferenceId() != null ? request.getReferenceId() : item.getReferenceId();
                String newTitle = request.getTitle() != null ? request.getTitle() : item.getTitle();
                validateReference(newType, newRefId, newTitle);
                item.setItemType(newType);
                item.setReferenceId(newRefId);
                item.setTitle(newTitle);
                item.setSource(newRefId != null ? "library" : "custom");
            }
            if (request.getDayNumber() != null) {
                item.setDayNumber(request.getDayNumber());
            }
            if (request.getStartTime() != null) {
                item.setStartTime(request.getStartTime());
            }
            if (request.getNotes() != null) {
                item.setNotes(request.getNotes());
            }
            if (request.getLongDescription() != null) {
                item.setLongDescription(request.getLongDescription());
            }
            if (request.getPrice() != null) {
                item.setPrice(request.getPrice());
            }
            if (request.getStatus() != null) {
                item.setStatus(request.getStatus());
            }
        }

        ItineraryItem saved = itineraryItemRepository.save(item);
        if (request.getTransportDetail() != null) {
            saveTransportDetail(saved, request.getTransportDetail());
        }
        if (request.getHotelDetail() != null) {
            saveHotelDetail(saved, request.getHotelDetail());
        }

        if (previousStatus != null && !previousStatus.equals(saved.getStatus())) {
            recordItemStatusChange(saved, previousStatus);
            // Reflects the changed (or reverted) price contribution in every
            // quote on this itinerary automatically — same computation
            // "Compute pricing" already runs, just re-triggered with each
            // quote's own last-used parameters.
            recomputeQuotesForItinerary(saved);
        }
        return saved;
    }

    private void recordItemStatusChange(ItineraryItem item, String previousStatus) {
        String prefix = item.getItemType().toUpperCase();
        Object newValue = BookingStatus.DROP.equals(item.getStatus())
                ? Map.of(
                        "status", item.getStatus(),
                        "reason", item.getDroppingReason() != null ? item.getDroppingReason() : "",
                        "cancellationCharge", item.getCancellationChargeInr() != null ? item.getCancellationChargeInr() : BigDecimal.ZERO)
                : item.getStatus();
        auditLogService.record("Escape", item.getItinerary().getEscape().getSeqp(),
                prefix + (BookingStatus.DROP.equals(item.getStatus()) ? "_DROPPED" : "_STATUS_CHANGED"),
                previousStatus, newValue);
    }

    /**
     * Upserts the 1:1 transport detail row and fully replaces its legs
     * (delete-and-reinsert — the form always resubmits the whole leg list,
     * so there's no partial-update case to reconcile against).
     */
    private void saveTransportDetail(ItineraryItem item, TransportDetailDTO dto) {
        ItineraryItemTransportDetail detail = transportDetailRepository.findByItineraryItem_Seqp(item.getSeqp())
                .orElseGet(() -> ItineraryItemTransportDetail.builder().itineraryItem(item).build());
        detail.setItineraryItem(item);
        detail.setModeCode(dto.getModeCode());
        detail.setVehicleTypeCode(dto.getVehicleTypeCode());
        detail.setPrice(dto.getPrice());
        detail.setTripType(dto.getTripType());
        detail.setCostPrice(dto.getCostPrice());
        detail.setCostPricePerPerson(dto.getCostPricePerPerson());
        detail.setSellingPrice(dto.getSellingPrice());
        detail.setSellingPricePerPerson(dto.getSellingPricePerPerson());
        detail.setAdultsCount(dto.getAdultsCount());
        detail.setChildrenCount(dto.getChildrenCount());
        detail.setInfantsCount(dto.getInfantsCount());
        detail.setAdditionalOptions(dto.getAdditionalOptions());
        transportDetailRepository.save(detail);

        transportLegRepository.deleteAllByItineraryItem_Seqp(item.getSeqp());
        if (dto.getLegs() != null && !dto.getLegs().isEmpty()) {
            List<ItineraryItemTransportLeg> legs = new ArrayList<>();
            for (int i = 0; i < dto.getLegs().size(); i++) {
                TransportLegDTO legDto = dto.getLegs().get(i);
                legs.add(ItineraryItemTransportLeg.builder()
                        .itineraryItem(item)
                        .legOrder(legDto.getLegOrder() != null ? legDto.getLegOrder() : i)
                        .direction(legDto.getDirection())
                        .departureAirport(legDto.getDepartureAirport())
                        .departureTerminal(legDto.getDepartureTerminal())
                        .departureTime(legDto.getDepartureTime())
                        .arrivalAirport(legDto.getArrivalAirport())
                        .arrivalTerminal(legDto.getArrivalTerminal())
                        .arrivalTime(legDto.getArrivalTime())
                        .flightNumber(legDto.getFlightNumber())
                        .build());
            }
            transportLegRepository.saveAll(legs);
        }
    }

    /**
     * Loads the transport detail + legs for one item, mapped to the response
     * DTO shape. Only worth calling for transport-like items — see
     * ItineraryItemServiceImpl, which gates this by itemType to avoid an
     * unnecessary query per non-transport item.
     */
    public TransportDetailDTO getTransportDetail(ItineraryItem item) {
        return transportDetailRepository.findByItineraryItem_Seqp(item.getSeqp())
                .map(detail -> {
                    TransportDetailDTO dto = new TransportDetailDTO();
                    dto.setModeCode(detail.getModeCode());
                    dto.setVehicleTypeCode(detail.getVehicleTypeCode());
                    dto.setPrice(detail.getPrice());
                    dto.setTripType(detail.getTripType());
                    dto.setCostPrice(detail.getCostPrice());
                    dto.setCostPricePerPerson(detail.getCostPricePerPerson());
                    dto.setSellingPrice(detail.getSellingPrice());
                    dto.setSellingPricePerPerson(detail.getSellingPricePerPerson());
                    dto.setAdultsCount(detail.getAdultsCount());
                    dto.setChildrenCount(detail.getChildrenCount());
                    dto.setInfantsCount(detail.getInfantsCount());
                    dto.setAdditionalOptions(detail.getAdditionalOptions());
                    dto.setLegs(transportLegRepository.findAllByItineraryItem_SeqpOrderByLegOrderAsc(item.getSeqp())
                            .stream().map(this::toLegDto).toList());
                    return dto;
                })
                .orElse(null);
    }

    private TransportLegDTO toLegDto(ItineraryItemTransportLeg leg) {
        TransportLegDTO dto = new TransportLegDTO();
        dto.setLegOrder(leg.getLegOrder());
        dto.setDirection(leg.getDirection());
        dto.setDepartureAirport(leg.getDepartureAirport());
        dto.setDepartureTerminal(leg.getDepartureTerminal());
        dto.setDepartureTime(leg.getDepartureTime());
        dto.setArrivalAirport(leg.getArrivalAirport());
        dto.setArrivalTerminal(leg.getArrivalTerminal());
        dto.setArrivalTime(leg.getArrivalTime());
        dto.setFlightNumber(leg.getFlightNumber());
        return dto;
    }

    /**
     * Upserts the 1:1 hotel detail row. Two very different modes hang off
     * this one method:
     * <ul>
     *   <li><b>Normal save</b> (new booking, or an update that isn't a Drop)
     *   — same as before: full field patch + inclusions delete-and-reinsert.
     *   A brand-new booking always lands on Initialize regardless of what
     *   the client sent, so status can never be picked at creation time.</li>
     *   <li><b>Drop</b> — an existing booking whose status is being set to
     *   Drop. Only status/droppingReason/cancellationChargeInr are written;
     *   every normal booking field (room type, occupancy, price, inclusions)
     *   is left exactly as it was.</li>
     * </ul>
     * Any actual status change (in either mode) is audit-logged against the
     * owning Escape, and triggers a quote recompute so the Quotation/billing
     * total picks up the change immediately — the same computation the
     * "Compute pricing" button already runs, just re-triggered automatically
     * with each quote's own last-used parameters.
     */
    private void saveHotelDetail(ItineraryItem item, HotelDetailDTO dto) {
        Optional<ItineraryItemHotelDetail> existing = hotelDetailRepository.findByItineraryItem_Seqp(item.getSeqp());
        ItineraryItemHotelDetail detail = existing.orElseGet(() -> ItineraryItemHotelDetail.builder().itineraryItem(item).build());
        detail.setItineraryItem(item);
        String previousStatus = existing.map(ItineraryItemHotelDetail::getStatus).orElse(null);
        boolean isNew = existing.isEmpty();
        boolean droppingNow = !isNew && BookingStatus.DROP.equals(dto.getStatus());

        if (droppingNow) {
            if (dto.getDroppingReason() == null || dto.getDroppingReason().isBlank()) {
                throw new BadRequestException("Dropping reason is required to mark a hotel as Drop");
            }
            detail.setStatus(BookingStatus.DROP);
            detail.setDroppingReason(dto.getDroppingReason());
            detail.setCancellationChargeInr(dto.getCancellationCharge());
            hotelDetailRepository.save(detail);
        } else {
            if (isNew) {
                detail.setStatus(BookingStatus.INITIALIZE);
            } else if (dto.getStatus() != null) {
                detail.setStatus(dto.getStatus());
            }
            validateHotelNights(item, dto.getNights());
            detail.setMealPlan(resolveMealPlan(dto.getMealPlanId()));
            detail.setRoomType(resolveRoomType(dto.getRoomTypeId()));
            detail.setNights(dto.getNights() != null ? dto.getNights() : 1);
            detail.setPaxPerRoom(dto.getPaxPerRoom());
            detail.setRoomCount(dto.getRoomCount());
            detail.setAdultsWithExtraBed(dto.getAdultsWithExtraBed());
            detail.setChildrenWithExtraBed(dto.getChildrenWithExtraBed());
            detail.setChildrenNoBed(dto.getChildrenNoBed());
            detail.setComplimentaryChildCount(dto.getComplimentaryChildCount());
            detail.setPrice(dto.getPrice());
            detail.setTotalPrice(dto.getTotalPrice());
            hotelDetailRepository.save(detail);

            hotelInclusionRepository.deleteAllByItineraryItem_Seqp(item.getSeqp());
            if (dto.getInclusions() != null && !dto.getInclusions().isEmpty()) {
                List<ItineraryItemHotelInclusion> inclusions = dto.getInclusions().stream()
                        .map(inclusionDto -> ItineraryItemHotelInclusion.builder()
                                .itineraryItem(item)
                                .service(inclusionDto.getService())
                                .startTime(inclusionDto.getStartTime())
                                .durationMinutes(inclusionDto.getDurationMinutes())
                                .totalPrice(inclusionDto.getTotalPrice())
                                .comments(inclusionDto.getComments())
                                .build())
                        .toList();
                hotelInclusionRepository.saveAll(inclusions);
            }
        }

        if (previousStatus != null && !previousStatus.equals(detail.getStatus())) {
            recordHotelStatusChange(item, detail, previousStatus);
            // Any status change affects what this hotel contributes to
            // pricing (Drop, or reverting a Drop back to Booked/Initialize)
            // — re-run the same computation "Compute pricing" already does,
            // for every quote on this itinerary, using each quote's own
            // last-used parameters so nothing needs re-entering.
            recomputeQuotesForItinerary(item);
        }
    }

    private void recordHotelStatusChange(ItineraryItem item, ItineraryItemHotelDetail detail, String previousStatus) {
        Object newValue = BookingStatus.DROP.equals(detail.getStatus())
                ? Map.of(
                        "status", detail.getStatus(),
                        "reason", detail.getDroppingReason() != null ? detail.getDroppingReason() : "",
                        "cancellationCharge", detail.getCancellationChargeInr() != null ? detail.getCancellationChargeInr() : BigDecimal.ZERO)
                : detail.getStatus();
        auditLogService.record("Escape", item.getItinerary().getEscape().getSeqp(),
                BookingStatus.DROP.equals(detail.getStatus()) ? "HOTEL_DROPPED" : "HOTEL_STATUS_CHANGED",
                previousStatus, newValue);
    }

    private void recomputeQuotesForItinerary(ItineraryItem item) {
        List<Quote> quotes = quoteRepository.findAllByOrgIdAndItinerary_Seqp(item.getOrgId(), item.getItinerary().getSeqp());
        for (Quote quote : quotes) {
            QuoteComputeRequestDTO request = new QuoteComputeRequestDTO();
            request.setTaxProfileUid(quote.getTaxProfileId());
            request.setTcsRatePercent(quote.getTcsRatePercent());
            request.setDiscountType(quote.getDiscountType());
            request.setDiscountValue(quote.getDiscountValue());
            request.setDisplayCurrencyCode(quote.getCurrencyCode());
            request.setFxRateSnapshot(quote.getFxRateSnapshot());
            quoteComputationService.compute(quote.getUid(), request);
        }
    }

    /**
     * A hotel stay's nights are capped by what's actually left of the
     * escape's total hotel-night budget — Escape.numberOfDays - 1 (see
     * EscapeHelper's endDate calculation: Day 1 is the start date itself, so
     * an N-day trip has N-1 nights to fill with stays), minus nights already
     * committed to this itinerary's OTHER hotel items. Mirrors
     * HotelDetailFields' frontend check so a direct API call can't save what
     * the UI would have blocked.
     */
    private void validateHotelNights(ItineraryItem item, Integer requestedNights) {
        int nights = requestedNights != null ? requestedNights : 1;
        if (nights <= 0) {
            throw new BadRequestException("No. of Night must be at least 1");
        }
        Escape escape = item.getItinerary().getEscape();
        Integer numberOfDays = escape.getNumberOfDays();
        int totalAvailable = numberOfDays != null ? Math.max(numberOfDays - 1, 0) : 0;
        int usedByOtherHotels = hotelDetailRepository.sumNightsForItineraryExcludingItem(
                item.getItinerary().getSeqp(), item.getSeqp());
        int remaining = totalAvailable - usedByOtherHotels;
        if (nights > remaining) {
            throw new BadRequestException(remaining <= 0
                    ? "No nights remaining for additional hotels in this escape."
                    : "Only " + remaining + " night" + (remaining == 1 ? "" : "s") + " remaining for this escape's hotels.");
        }
    }

    private MealPlan resolveMealPlan(UUID mealPlanUid) {
        if (mealPlanUid == null) return null;
        return mealPlanRepository.findByUid(mealPlanUid)
                .orElseThrow(() -> new NotFoundException("MealPlan not found: " + mealPlanUid));
    }

    private RoomType resolveRoomType(UUID roomTypeUid) {
        if (roomTypeUid == null) return null;
        return roomTypeRepository.findByUid(roomTypeUid)
                .orElseThrow(() -> new NotFoundException("RoomType not found: " + roomTypeUid));
    }

    /**
     * Loads the hotel detail + inclusions for one item, mapped to the
     * response DTO shape. Only worth calling for hotel items — see
     * ItineraryItemServiceImpl, which gates this by itemType.
     */
    public HotelDetailDTO getHotelDetail(ItineraryItem item) {
        return hotelDetailRepository.findByItineraryItem_Seqp(item.getSeqp())
                .map(detail -> {
                    HotelDetailDTO dto = new HotelDetailDTO();
                    dto.setMealPlanId(detail.getMealPlan() != null ? detail.getMealPlan().getUid() : null);
                    dto.setRoomTypeId(detail.getRoomType() != null ? detail.getRoomType().getUid() : null);
                    dto.setNights(detail.getNights());
                    dto.setPaxPerRoom(detail.getPaxPerRoom());
                    dto.setRoomCount(detail.getRoomCount());
                    dto.setAdultsWithExtraBed(detail.getAdultsWithExtraBed());
                    dto.setChildrenWithExtraBed(detail.getChildrenWithExtraBed());
                    dto.setChildrenNoBed(detail.getChildrenNoBed());
                    dto.setComplimentaryChildCount(detail.getComplimentaryChildCount());
                    dto.setPrice(detail.getPrice());
                    dto.setTotalPrice(detail.getTotalPrice());
                    dto.setInclusions(hotelInclusionRepository.findAllByItineraryItem_SeqpOrderBySeqpAsc(item.getSeqp())
                            .stream().map(this::toInclusionDto).toList());
                    dto.setStatus(detail.getStatus());
                    dto.setDroppingReason(detail.getDroppingReason());
                    dto.setCancellationCharge(detail.getCancellationChargeInr());
                    return dto;
                })
                .orElse(null);
    }

    private HotelInclusionDTO toInclusionDto(ItineraryItemHotelInclusion inclusion) {
        HotelInclusionDTO dto = new HotelInclusionDTO();
        dto.setService(inclusion.getService());
        dto.setStartTime(inclusion.getStartTime());
        dto.setDurationMinutes(inclusion.getDurationMinutes());
        dto.setTotalPrice(inclusion.getTotalPrice());
        dto.setComments(inclusion.getComments());
        return dto;
    }

    public void delete(UUID uid) {
        ItineraryItem item = getByUid(uid);
        transportLegRepository.deleteAllByItineraryItem_Seqp(item.getSeqp());
        transportDetailRepository.findByItineraryItem_Seqp(item.getSeqp()).ifPresent(transportDetailRepository::delete);
        hotelInclusionRepository.deleteAllByItineraryItem_Seqp(item.getSeqp());
        hotelDetailRepository.findByItineraryItem_Seqp(item.getSeqp()).ifPresent(hotelDetailRepository::delete);
        itineraryItemRepository.delete(item);
    }

    public List<ItineraryItem> reorder(ItineraryItemReorderRequestDTO request) {
        Itinerary itinerary = itineraryHelper.getByUid(request.getItineraryUid());
        List<ItineraryItem> items = itineraryItemRepository.findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(itinerary.getSeqp());

        for (int i = 0; i < request.getOrderedItemUids().size(); i++) {
            UUID uid = request.getOrderedItemUids().get(i);
            int position = i;
            items.stream()
                    .filter(item -> item.getUid().equals(uid))
                    .findFirst()
                    .ifPresent(item -> item.setSortOrder(position));
        }

        return itineraryItemRepository.saveAll(items);
    }

    public String resolveLabel(ItineraryItem item) {
        if (item.getReferenceId() == null) {
            return item.getTitle();
        }
        RefKind kind = REF_KIND_BY_TYPE.get(item.getItemType());
        String resolved = kind == null ? null : switch (kind) {
            case HOTEL -> hotelRepository.findByUid(item.getReferenceId()).map(h -> h.getName()).orElse(null);
            case ACTIVITY -> activityRepository.findByUid(item.getReferenceId()).map(a -> a.getName()).orElse(null);
            case TRANSPORT -> transportRepository.findByUid(item.getReferenceId())
                    .map(t -> t.getModeCode() + (t.getVehicleTypeCode() != null ? " — " + t.getVehicleTypeCode() : ""))
                    .orElse(null);
            case SERVICE_PROVIDER -> serviceProviderRepository.findByUid(item.getReferenceId()).map(p -> p.getName()).orElse(null);
            case NONE -> null;
        };
        if (resolved != null) {
            return resolved;
        }
        return item.getTitle() != null ? item.getTitle() : "(deleted " + item.getItemType() + ")";
    }

    /**
     * Batch version of resolveLabel for a whole list — one query per RefKind
     * (4 total) instead of one query per item. Keyed by the item's own uid
     * (not referenceId) since the title-fallback is per-item and items with
     * no referenceId at all still need a key to store their label under.
     */
    public Map<UUID, String> resolveLabels(List<ItineraryItem> items) {
        List<ItineraryItem> withRef = items.stream().filter(i -> i.getReferenceId() != null).toList();

        List<UUID> hotelIds = byKind(withRef, RefKind.HOTEL);
        List<UUID> activityIds = byKind(withRef, RefKind.ACTIVITY);
        List<UUID> transportIds = byKind(withRef, RefKind.TRANSPORT);
        List<UUID> providerIds = byKind(withRef, RefKind.SERVICE_PROVIDER);

        Map<UUID, String> byReferenceId = new HashMap<>();
        if (!hotelIds.isEmpty()) {
            hotelRepository.findAllByUidIn(hotelIds).forEach(h -> byReferenceId.put(h.getUid(), h.getName()));
        }
        if (!activityIds.isEmpty()) {
            activityRepository.findAllByUidIn(activityIds).forEach(a -> byReferenceId.put(a.getUid(), a.getName()));
        }
        if (!transportIds.isEmpty()) {
            transportRepository.findAllByUidIn(transportIds).forEach(t ->
                    byReferenceId.put(t.getUid(), t.getModeCode() + (t.getVehicleTypeCode() != null ? " — " + t.getVehicleTypeCode() : "")));
        }
        if (!providerIds.isEmpty()) {
            serviceProviderRepository.findAllByUidIn(providerIds).forEach(p -> byReferenceId.put(p.getUid(), p.getName()));
        }

        Map<UUID, String> byItemUid = new HashMap<>();
        for (ItineraryItem item : items) {
            String fromLibrary = item.getReferenceId() != null ? byReferenceId.get(item.getReferenceId()) : null;
            String label = fromLibrary != null ? fromLibrary
                    : (item.getTitle() != null ? item.getTitle() : "(deleted " + item.getItemType() + ")");
            byItemUid.put(item.getUid(), label);
        }
        return byItemUid;
    }

    private List<UUID> byKind(List<ItineraryItem> items, RefKind kind) {
        return items.stream()
                .filter(i -> REF_KIND_BY_TYPE.get(i.getItemType()) == kind)
                .map(ItineraryItem::getReferenceId)
                .toList();
    }

    private void validateReference(String itemType, UUID referenceId, String title) {
        if (!VALID_TYPES.contains(itemType)) {
            throw new BadRequestException("itemType must be one of: " + VALID_TYPES);
        }
        // Activity items are always library-linked from the day planner's
        // "Add Activity" form (no free-text title field for that type) —
        // enforce it server-side too rather than trusting the client.
        if ("activity".equals(itemType) && referenceId == null) {
            throw new BadRequestException("referenceId is required for activity items");
        }
        boolean hasTitle = title != null && !title.isBlank();
        if (referenceId == null) {
            if (!hasTitle) {
                throw new BadRequestException("Either title or referenceId is required");
            }
            return;
        }
        RefKind kind = REF_KIND_BY_TYPE.get(itemType);
        boolean exists = switch (kind) {
            case HOTEL -> hotelRepository.findByUid(referenceId).isPresent();
            case ACTIVITY -> activityRepository.findByUid(referenceId).isPresent();
            case TRANSPORT -> transportRepository.findByUid(referenceId).isPresent();
            case SERVICE_PROVIDER -> serviceProviderRepository.findByUid(referenceId).isPresent();
            case NONE -> false;
        };
        if (!exists) {
            throw new NotFoundException("No " + itemType + " reference found with id: " + referenceId);
        }
    }

    private ItineraryItem getByUid(UUID uid) {
        ItineraryItem item = itineraryItemRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Itinerary item not found"));
        orgAccessGuard.requireAccessToOrg(item.getOrgId());
        return item;
    }

    private int nextSortOrder(Long itinerarySeqp) {
        List<ItineraryItem> existing = itineraryItemRepository.findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(itinerarySeqp);
        return existing.stream().mapToInt(ItineraryItem::getSortOrder).max().orElse(-1) + 1;
    }
}
