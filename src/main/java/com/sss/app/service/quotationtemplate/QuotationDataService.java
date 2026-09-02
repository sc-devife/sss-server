package com.sss.app.service.quotationtemplate;

import com.sss.app.AddressType;
import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryResponseDTO;
import com.sss.app.dto.itinerary.TransportLegDTO;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;
import com.sss.app.dto.quote.QuoteResponseDTO;
import com.sss.app.dto.traveller.TravellerResponseDTO;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.roomtype.RoomType;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.library.mealplan.MealPlanRepository;
import com.sss.app.repository.library.roomtype.RoomTypeRepository;
import com.sss.app.service.AddressService;
import com.sss.app.service.OrganizationsService;
import com.sss.app.service.deal.DealService;
import com.sss.app.service.escape.EscapeService;
import com.sss.app.service.itinerary.ItineraryContentItemService;
import com.sss.app.service.itinerary.ItineraryItemService;
import com.sss.app.service.itinerary.ItineraryService;
import com.sss.app.service.payment.PaymentMilestoneService;
import com.sss.app.service.quote.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.sss.app.service.quotationtemplate.QuotationDataMapUtil.map;

/**
 * Assembles the SAME map shape as SampleQuotationDataService, but from real
 * data for one Escape — reusing existing services (Escape/Itinerary/Quote/
 * Deal/PaymentMilestone/Organizations) instead of new queries, so this stays
 * in sync with those modules automatically.
 */
@Service
@RequiredArgsConstructor
public class QuotationDataService {

    private final EscapeService escapeService;
    private final ItineraryService itineraryService;
    private final ItineraryItemService itineraryItemService;
    private final ItineraryContentItemService itineraryContentItemService;
    private final QuoteService quoteService;
    private final DealService dealService;
    private final PaymentMilestoneService paymentMilestoneService;
    private final OrganizationsService organizationsService;
    private final AddressService addressService;
    private final MealPlanRepository mealPlanRepository;
    private final RoomTypeRepository roomTypeRepository;

    public Map<String, Object> buildData(UUID escapeUid) {
        EscapeResponseDTO escape = escapeService.getEscapeById(escapeUid);
        OrganizationsDto org = organizationsService.getMyOrganization();

        List<ItineraryResponseDTO> itineraries = itineraryService.getAllForEscape(escapeUid);
        ItineraryResponseDTO itinerary = itineraries.stream()
                .max(Comparator.comparing(ItineraryResponseDTO::getVersion))
                .orElse(null);

        List<Map<String, Object>> days = List.of();
        List<Map<String, Object>> inclusions = List.of();
        List<Map<String, Object>> exclusions = List.of();
        List<Map<String, Object>> terms = List.of();
        Map<String, Object> pricing = map();

        if (itinerary != null) {
            List<ItineraryItemResponseDTO> items = itineraryItemService.getAllForItinerary(itinerary.getUid());
            Map<UUID, String> mealPlanNames = resolveMealPlanNames(items);
            Map<UUID, String> roomTypeNames = resolveRoomTypeNames(items);
            days = items.stream()
                    .collect(Collectors.groupingBy(ItineraryItemResponseDTO::getDayNumber, Collectors.toList()))
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> map(
                            "dayNumber", e.getKey(),
                            "items", e.getValue().stream()
                                    .sorted(Comparator.comparing(ItineraryItemResponseDTO::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                                    .map(item -> toItemMap(item, mealPlanNames, roomTypeNames))
                                    .toList()
                    ))
                    .toList();

            List<ItineraryContentItemResponseDTO> content = itineraryContentItemService.getAllForItinerary(itinerary.getUid());
            inclusions = content.stream().filter(c -> "INCLUSION".equalsIgnoreCase(c.getType())).map(this::toContentMap).toList();
            exclusions = content.stream().filter(c -> "EXCLUSION".equalsIgnoreCase(c.getType())).map(this::toContentMap).toList();
            terms = content.stream().filter(c -> "TERMS".equalsIgnoreCase(c.getType())).map(this::toContentMap).toList();

            List<QuoteResponseDTO> quotes = quoteService.getAllForItinerary(itinerary.getUid());
            // A quotation should reflect what was actually agreed, not just
            // whichever quote happens to have the highest version — an
            // itinerary can have several independent quotes (each starting
            // at version 1), so "latest version" alone doesn't identify the
            // accepted one. Prefer the accepted quote (most recently updated,
            // in case more than one is ever marked accepted); only fall back
            // to "latest version" for an itinerary with no accepted quote yet
            // (e.g. previewing a still-draft quotation).
            QuoteResponseDTO latestQuote = quotes.stream()
                    .filter(q -> "accepted".equals(q.getStatus()))
                    .max(Comparator.comparing(QuoteResponseDTO::getUpdatedAt))
                    .or(() -> quotes.stream().max(Comparator.comparing(QuoteResponseDTO::getVersion)))
                    .orElse(null);
            if (latestQuote != null) {
                pricing = map(
                        "currencyCode", latestQuote.getCurrencyCode(),
                        "subtotal", latestQuote.getSubtotalInr(),
                        "tax", latestQuote.getTaxAmountInr(),
                        "total", latestQuote.getTotalInr(),
                        "discountType", latestQuote.getDiscountType(),
                        "discountValue", latestQuote.getDiscountValue()
                );
            }
        }

        List<Map<String, Object>> milestones;
        try {
            UUID dealUid = dealService.getForEscape(escapeUid).getUid();
            milestones = paymentMilestoneService.getAllForDeal(dealUid).stream()
                    .map(this::toMilestoneMap)
                    .toList();
        } catch (NotFoundException e) {
            milestones = List.of();
        }

        AddressDto billingAddress = resolveBillingAddress(org.getUid());

        return map(
                "organization", map(
                        "name", org.getDisplay_name() != null ? org.getDisplay_name() : org.getRegistered_name(),
                        "logoUrl", resolveLogoUrl(org.getLogo_file()),
                        "tagline", org.getTagline(),
                        "contactEmail", org.getBusiness_email(),
                        "contactPhone", org.getSupport_ph_num(),
                        "websiteUrl", org.getWebsite_url(),
                        "gstin", billingAddress == null ? null : billingAddress.getGstin(),
                        "address", billingAddress == null ? map() : map(
                                "streetFirst", billingAddress.getStreetFirst(),
                                "streetSecond", billingAddress.getStreetSecond(),
                                "landMark", billingAddress.getLandMark(),
                                "city", billingAddress.getCity(),
                                "state", billingAddress.getState(),
                                "country", billingAddress.getCountry(),
                                "zipCode", billingAddress.getZipCode(),
                                "contactNumber", billingAddress.getContactNumber(),
                                "contactEmail", billingAddress.getContactEmail()
                        )
                ),
                "lead", escape.getLead() == null ? map() : map(
                        "name", escape.getLead().getName(),
                        "email", escape.getLead().getEmail(),
                        "phone", escape.getLead().getPhone()
                ),
                "travellers", escape.getTravellers() == null ? List.of() : escape.getTravellers().stream()
                        .map(t -> toTravellerMap(t, escape.getPrimaryTravellerUid()))
                        .toList(),
                "escapePoints", escape.getEscapePoints() == null ? List.of() : escape.getEscapePoints().stream()
                        .map(this::toEscapePointMap)
                        .toList(),
                "startDate", escape.getStartDate(),
                "endDate", escape.getEndDate(),
                "numberOfDays", escape.getNumberOfDays(),
                "days", days,
                "pricing", pricing,
                "inclusions", inclusions,
                "exclusions", exclusions,
                "terms", terms,
                "payment", map("milestones", milestones)
        );
    }

    // Org logos are uploaded via CloudinaryService (always an absolute
    // https://res.cloudinary.com/... URL) — this only guards against a
    // legacy relative "/files/..." path pre-dating that migration, which the
    // backend has no public base URL to resolve, so it's dropped rather than
    // rendered as a broken image.
    private String resolveLogoUrl(String logoFile) {
        return logoFile != null && (logoFile.startsWith("http://") || logoFile.startsWith("https://")) ? logoFile : null;
    }

    // Prefers a BILLING-typed address (where GSTIN actually lives) over the
    // org's other address(es); among BILLING addresses prefers the one
    // marked primary. Falls back to any address, then null if the org has
    // none configured yet — a quotation should still render without one.
    private AddressDto resolveBillingAddress(String orgUid) {
        List<AddressDto> addresses = addressService.getAddressesForOrg(orgUid);
        return addresses.stream()
                .filter(a -> a.getAddressTypes() != null && a.getAddressTypes().contains(AddressType.BILLING))
                .max(Comparator.comparing(a -> Boolean.TRUE.equals(a.getPrimaryAddress())))
                .or(() -> addresses.stream().findFirst())
                .orElse(null);
    }

    private Map<String, Object> toTravellerMap(TravellerResponseDTO t, UUID primaryTravellerUid) {
        String fullName = t.getLastName() != null && !t.getLastName().isBlank()
                ? t.getFirstName() + " " + t.getLastName()
                : t.getFirstName();
        return map(
                "name", fullName,
                "type", t.getType(),
                "email", t.getEmail(),
                "phone", t.getPhone(),
                "isPrimary", t.getUid() != null && t.getUid().equals(primaryTravellerUid)
        );
    }

    private Map<String, Object> toEscapePointMap(EscapePointResponseDto ep) {
        return map(
                "name", ep.getName(),
                "locationLabel", ep.getLocationLabel(),
                "description", ep.getDescription(),
                "imageUrl", ep.getImages() != null && !ep.getImages().isEmpty() ? ep.getImages().get(0) : null
        );
    }

    // Batched once per buildData() call (not per item) to avoid an N+1 query
    // per hotel line item — findAllByUidIn resolves every mealPlanId used
    // across the whole itinerary in one round trip.
    private Map<UUID, String> resolveMealPlanNames(List<ItineraryItemResponseDTO> items) {
        Set<UUID> ids = items.stream()
                .map(ItineraryItemResponseDTO::getHotelDetail)
                .filter(h -> h != null && h.getMealPlanId() != null)
                .map(h -> h.getMealPlanId())
                .collect(Collectors.toCollection(HashSet::new));
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<UUID, String> names = new HashMap<>();
        for (MealPlan mp : mealPlanRepository.findAllByUidIn(ids)) {
            names.put(mp.getUid(), mp.getCode() + " — " + mp.getName());
        }
        return names;
    }

    private Map<UUID, String> resolveRoomTypeNames(List<ItineraryItemResponseDTO> items) {
        Set<UUID> ids = items.stream()
                .map(ItineraryItemResponseDTO::getHotelDetail)
                .filter(h -> h != null && h.getRoomTypeId() != null)
                .map(h -> h.getRoomTypeId())
                .collect(Collectors.toCollection(HashSet::new));
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<UUID, String> names = new HashMap<>();
        for (RoomType rt : roomTypeRepository.findAllByUidIn(ids)) {
            names.put(rt.getUid(), rt.getName());
        }
        return names;
    }

    private Map<String, Object> toItemMap(ItineraryItemResponseDTO item, Map<UUID, String> mealPlanNames, Map<UUID, String> roomTypeNames) {
        Map<String, Object> m = map(
                "itemType", item.getItemType(),
                "title", item.getTitle() != null ? item.getTitle() : item.getReferenceLabel(),
                "startTime", item.getStartTime(),
                "notes", item.getNotes(),
                "price", item.getPrice()
        );
        if (item.getHotelDetail() != null) {
            m.put("hotel", map(
                    "roomCount", item.getHotelDetail().getRoomCount(),
                    "paxPerRoom", item.getHotelDetail().getPaxPerRoom(),
                    "totalPrice", item.getHotelDetail().getTotalPrice(),
                    "mealPlanName", mealPlanNames.get(item.getHotelDetail().getMealPlanId()),
                    "roomTypeName", roomTypeNames.get(item.getHotelDetail().getRoomTypeId())
            ));
        }
        if (item.getTransportDetail() != null) {
            List<TransportLegDTO> legs = item.getTransportDetail().getLegs();
            // sellingPrice is only populated for flight bookings (Section 6's
            // pricing block); simpler modes (car/bus/ferry) instead store
            // their price directly on TransportDetailDTO.price — fall back to
            // that so non-flight transport still shows an amount.
            Object displayPrice = item.getTransportDetail().getSellingPrice() != null
                    ? item.getTransportDetail().getSellingPrice()
                    : item.getTransportDetail().getPrice();
            m.put("transport", map(
                    "modeCode", item.getTransportDetail().getModeCode(),
                    "vehicleTypeCode", item.getTransportDetail().getVehicleTypeCode(),
                    "tripType", item.getTransportDetail().getTripType(),
                    "sellingPrice", displayPrice,
                    "legs", legs == null ? List.of() : legs.stream().map(this::toLegMap).toList()
            ));
        }
        return m;
    }

    private Map<String, Object> toLegMap(TransportLegDTO leg) {
        return map(
                "direction", leg.getDirection(),
                "departureAirport", leg.getDepartureAirport(),
                "departureTerminal", leg.getDepartureTerminal(),
                "departureTime", leg.getDepartureTime(),
                "arrivalAirport", leg.getArrivalAirport(),
                "arrivalTerminal", leg.getArrivalTerminal(),
                "arrivalTime", leg.getArrivalTime(),
                "flightNumber", leg.getFlightNumber()
        );
    }

    private Map<String, Object> toContentMap(ItineraryContentItemResponseDTO c) {
        return map("name", c.getName(), "contentHtml", c.getContentHtml());
    }

    private Map<String, Object> toMilestoneMap(PaymentMilestoneResponseDTO p) {
        return map(
                "label", p.getLabel(),
                "dueDate", p.getDueDate(),
                "amount", p.getAmountInr(),
                "amountPaid", p.getAmountPaidInr(),
                "status", p.getStatus(),
                "method", p.getPaymentMethod(),
                "reference", p.getPaymentReference()
        );
    }
}
