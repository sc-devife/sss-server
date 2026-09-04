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
import com.sss.app.entity.library.activity.Activity;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.entity.library.mealplan.MealPlan;
import com.sss.app.entity.library.roomtype.RoomType;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.library.activity.ActivityRepository;
import com.sss.app.repository.library.hotel.HotelRepository;
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
    private final HotelRepository hotelRepository;
    private final ActivityRepository activityRepository;

    public Map<String, Object> buildData(UUID escapeUid) {
        EscapeResponseDTO escape = escapeService.getEscapeById(escapeUid);
        OrganizationsDto org = organizationsService.getMyOrganization();

        List<ItineraryResponseDTO> itineraries = itineraryService.getAllForEscape(escapeUid);
        ItineraryResponseDTO itinerary = itineraries.stream()
                .max(Comparator.comparing(ItineraryResponseDTO::getVersion))
                .orElse(null);

        List<Map<String, Object>> days = List.of();
        List<Map<String, Object>> transportRows = List.of();
        List<Map<String, Object>> inclusions = List.of();
        List<Map<String, Object>> exclusions = List.of();
        List<Map<String, Object>> terms = List.of();
        List<Map<String, Object>> specialInclusions = List.of();
        Map<String, Object> pricing = map();

        if (itinerary != null) {
            List<ItineraryItemResponseDTO> items = itineraryItemService.getAllForItinerary(itinerary.getUid());
            Map<UUID, String> mealPlanNames = resolveMealPlanNames(items);
            Map<UUID, String> roomTypeNames = resolveRoomTypeNames(items);
            Map<UUID, Hotel> hotels = resolveHotels(items);
            Map<UUID, Activity> activities = resolveActivities(items);
            days = items.stream()
                    .collect(Collectors.groupingBy(ItineraryItemResponseDTO::getDayNumber, Collectors.toList()))
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> {
                        // Real arithmetic on the escape's own startDate — not
                        // a stored/fabricated value — so a template can show
                        // "27 November 2026" per day.
                        Object date = escape.getStartDate() != null ? escape.getStartDate().plusDays(e.getKey() - 1) : null;
                        return map(
                                "dayNumber", e.getKey(),
                                "date", date,
                                "items", e.getValue().stream()
                                        .sorted(Comparator.comparing(ItineraryItemResponseDTO::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                                        // Mustache (the Java library used here) has no
                                        // parent-context access like Handlebars' "../" —
                                        // dayNumber/date are denormalized onto each item
                                        // itself so a template can show them without it.
                                        .map(item -> toItemMap(item, mealPlanNames, roomTypeNames, hotels, activities, e.getKey(), date))
                                        .toList()
                        );
                    })
                    .toList();

            // Flat, hotel-filtered row list for the Transportation &
            // Activities TABLE — only the first row of each day carries
            // dayOrdinalLabel/dayDateLabel/dayRowspan (a Mustache section on
            // dayOrdinalLabel then renders that <td rowspan="dayRowspan">
            // once and every other row in the day skips it), so the day
            // column visually merges instead of repeating "Day 1" on every row.
            transportRows = days.stream()
                    .flatMap(day -> {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> dayItems = (List<Map<String, Object>>) day.get("items");
                        List<Map<String, Object>> filtered = dayItems.stream().filter(m -> !m.containsKey("hotel")).toList();
                        if (filtered.isEmpty()) {
                            return java.util.stream.Stream.empty();
                        }
                        String ordinalLabel = dayOrdinalLabel((Integer) day.get("dayNumber"));
                        String dateLabel = dayDateLabel(day.get("date"));
                        return java.util.stream.IntStream.range(0, filtered.size())
                                .mapToObj(i -> {
                                    Map<String, Object> row = new HashMap<>(filtered.get(i));
                                    if (i == 0) {
                                        row.put("dayOrdinalLabel", ordinalLabel);
                                        row.put("dayDateLabel", dateLabel);
                                        row.put("dayRowspan", filtered.size());
                                    }
                                    return row;
                                });
                    })
                    .toList();

            // "other"-typed items are named, day-tagged extras with no
            // library/pricing shape (Visa, welcome gift, complimentary swing
            // dress...) — they already render inline in the day-wise
            // itinerary above via toItemMap, and are ALSO pulled out here
            // into their own list so a template can give them the
            // standalone "Special Inclusions" treatment travel-agency
            // quotations use, without duplicating any item-building logic.
            specialInclusions = items.stream()
                    .filter(i -> "other".equals(i.getItemType()))
                    .sorted(Comparator.comparing(ItineraryItemResponseDTO::getDayNumber))
                    .map(i -> map(
                            "dayNumber", i.getDayNumber(),
                            "title", i.getTitle() != null ? i.getTitle() : i.getReferenceLabel(),
                            "notes", i.getNotes()
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
                int paxCount = escape.getTravellers() != null ? escape.getTravellers().size() : 0;
                java.math.BigDecimal perPaxValue = paxCount > 0 && latestQuote.getTotalInr() != null
                        ? latestQuote.getTotalInr().divide(java.math.BigDecimal.valueOf(paxCount), 2, java.math.RoundingMode.HALF_UP)
                        : null;
                // Mustache can't format numbers (comma grouping, dropping
                // decimals) — pre-formatted here so the Quote Price section
                // can print "83,200" instead of a raw "83200.00".
                java.text.NumberFormat inrFormat = inrWholeFormat();
                pricing = map(
                        "quoteCode", latestQuote.getQuoteCode(),
                        // Same name shown in the Quotes section on the Escape
                        // page (see DocumentsCard.tsx) — reused as-is for the
                        // PDF download filename, never a separately generated value.
                        "quoteName", latestQuote.getName() != null ? latestQuote.getName() : "Quote " + latestQuote.getVersion(),
                        "currencyCode", latestQuote.getCurrencyCode(),
                        "subtotal", latestQuote.getSubtotalInr(),
                        "tax", latestQuote.getTaxAmountInr(),
                        "tcsRate", latestQuote.getTcsRatePercent(),
                        "tcs", latestQuote.getTcsAmountInr(),
                        "total", latestQuote.getTotalInr(),
                        "totalFormatted", latestQuote.getTotalInr() != null ? inrFormat.format(latestQuote.getTotalInr()) : null,
                        "discountType", latestQuote.getDiscountType(),
                        "discountValue", latestQuote.getDiscountValue(),
                        "paxCount", paxCount,
                        "perPax", perPaxValue,
                        "perPaxFormatted", perPaxValue != null ? inrFormat.format(perPaxValue) : null,
                        // Drives the PDF's combined "including GST & TCS" note —
                        // true whenever either tax is actually non-zero.
                        "hasTaxOrTcs", isPositive(latestQuote.getTaxAmountInr()) || isPositive(latestQuote.getTcsAmountInr())
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

        List<Map<String, Object>> escapePointMaps = escape.getEscapePoints() == null ? List.of() : escape.getEscapePoints().stream()
                .map(this::toEscapePointMap)
                .toList();

        return map(
                "organization", map(
                        "name", org.getDisplay_name() != null ? org.getDisplay_name() : org.getRegistered_name(),
                        "logoUrl", resolveLogoUrl(org.getLogo_file()),
                        "tagline", org.getTagline(),
                        // The org's own "About" text, set on the Organization
                        // profile page — reused verbatim for the quotation
                        // greeting, never a separately authored message.
                        "aboutText", org.getAbout_text(),
                        "contactEmail", org.getBusiness_email(),
                        "contactPhone", org.getSupport_ph_num(),
                        "websiteUrl", org.getWebsite_url(),
                        "instagramUrl", org.getInstagram_url(),
                        "linkedinUrl", org.getLinkedin_url(),
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
                "escapePoints", escapePointMaps,
                // Convenience alias for the first escape point — Mustache
                // (the Java library used here) has no numeric list-index
                // access like "escapePoints.0.name", only dotted paths
                // through nested maps, so a hero/trip-info section that only
                // ever shows the first destination needs this instead of
                // indexing into the list.
                "primaryEscapePoint", escapePointMaps.isEmpty() ? map() : escapePointMaps.get(0),
                "tripCode", escape.getTripCode(),
                "startDate", escape.getStartDate(),
                "endDate", escape.getEndDate(),
                "numberOfDays", escape.getNumberOfDays(),
                // Escape-level pax count, independent of whether a quote has
                // been computed yet (unlike pricing.paxCount, which only
                // exists once a quote does) — same travellers list, just
                // counted here for the trip-info header.
                "travellersCount", escape.getTravellers() == null ? 0 : escape.getTravellers().size(),
                "days", days,
                "transportRows", transportRows,
                "hasTransportRows", !transportRows.isEmpty(),
                // Drives whether a template's "Useful Links" block (at the
                // end of Terms & Conditions) renders at all — true whenever
                // at least one of the three org links is set, so a template
                // never prints an empty links section. Kept top-level (not
                // nested under "organization") because every template
                // references it as a bare {{#hasUsefulLinks}} section.
                "hasUsefulLinks", isNotBlank(org.getWebsite_url()) || isNotBlank(org.getInstagram_url()) || isNotBlank(org.getLinkedin_url()),
                "pricing", pricing,
                "inclusions", inclusions,
                "exclusions", exclusions,
                "terms", terms,
                "specialInclusions", specialInclusions,
                "payment", map("milestones", milestones)
        );
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isPositive(java.math.BigDecimal value) {
        return value != null && value.compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    private boolean isPositive(Integer value) {
        return value != null && value > 0;
    }

    // 1 -> "1st", 2 -> "2nd", 3 -> "3rd", 4 -> "4th", 11-13 -> "11th"/"12th"/"13th", ...
    private String ordinal(int n) {
        if (n % 100 >= 11 && n % 100 <= 13) {
            return n + "th";
        }
        return switch (n % 10) {
            case 1 -> n + "st";
            case 2 -> n + "nd";
            case 3 -> n + "rd";
            default -> n + "th";
        };
    }

    // "1st Day" / "Fri, 13 Oct" — two pre-formatted labels (kept separate,
    // not one combined string) so the Transportation & Activities section
    // can print the day number and date on their own lines, and group all
    // of that day's items under them, instead of Mustache (which can't
    // format dates or dedupe consecutive values on its own).
    private String dayOrdinalLabel(int dayNumber) {
        return ordinal(dayNumber) + " Day";
    }

    private String dayDateLabel(Object date) {
        if (date instanceof java.time.LocalDate d) {
            String weekday = d.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH);
            String month = d.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH);
            return weekday + ", " + d.getDayOfMonth() + " " + month;
        }
        return null;
    }

    private java.text.NumberFormat inrWholeFormat() {
        java.text.NumberFormat format = java.text.NumberFormat.getInstance(new java.util.Locale("en", "IN"));
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format;
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

    // Batched once per buildData() call — resolves every Hotel referenced by
    // a library-sourced hotel item in one round trip, mirroring
    // resolveMealPlanNames/resolveRoomTypeNames above.
    private Map<UUID, Hotel> resolveHotels(List<ItineraryItemResponseDTO> items) {
        List<UUID> ids = items.stream()
                .filter(i -> "hotel".equals(i.getItemType()) && i.getReferenceId() != null)
                .map(ItineraryItemResponseDTO::getReferenceId)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Hotel> byUid = new HashMap<>();
        for (Hotel hotel : hotelRepository.findAllByUidIn(ids)) {
            byUid.put(hotel.getUid(), hotel);
        }
        return byUid;
    }

    // Mirrors resolveHotels — batched once per buildData() call, resolves
    // every Activity referenced by a library-sourced activity item in one
    // round trip, so item-level activity duration can be surfaced without
    // an N+1 query per item.
    private Map<UUID, Activity> resolveActivities(List<ItineraryItemResponseDTO> items) {
        List<UUID> ids = items.stream()
                .filter(i -> "activity".equals(i.getItemType()) && i.getReferenceId() != null)
                .map(ItineraryItemResponseDTO::getReferenceId)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Activity> byUid = new HashMap<>();
        for (Activity activity : activityRepository.findAllByUidIn(ids)) {
            byUid.put(activity.getUid(), activity);
        }
        return byUid;
    }

    private Map<String, Object> toItemMap(ItineraryItemResponseDTO item, Map<UUID, String> mealPlanNames, Map<UUID, String> roomTypeNames, Map<UUID, Hotel> hotels, Map<UUID, Activity> activities, Integer dayNumber, Object date) {
        Map<String, Object> m = map(
                "itemType", item.getItemType(),
                // Denormalized from the enclosing day — see the buildData()
                // comment on why (no Mustache parent-context access).
                "dayNumber", dayNumber,
                "date", date,
                "title", item.getTitle() != null ? item.getTitle() : item.getReferenceLabel(),
                "startTime", item.getStartTime(),
                "notes", item.getNotes(),
                "longDescription", item.getLongDescription(),
                "price", item.getPrice()
        );
        if (item.getHotelDetail() != null) {
            Hotel hotel = item.getReferenceId() != null ? hotels.get(item.getReferenceId()) : null;
            Integer adultsExtraBed = item.getHotelDetail().getAdultsWithExtraBed();
            Integer childrenExtraBed = item.getHotelDetail().getChildrenWithExtraBed();
            Integer childrenNoBed = item.getHotelDetail().getChildrenNoBed();
            Integer complimentaryChild = item.getHotelDetail().getComplimentaryChildCount();
            // A hotel booking covers multiple consecutive nights, not a
            // single itinerary day — nights defaults to 1 for stays saved
            // before this field existed. checkOutDate is always derived
            // (never separately stored) so it can't drift out of sync with
            // the check-in day.
            int nights = item.getHotelDetail().getNights() != null ? item.getHotelDetail().getNights() : 1;
            Object checkOutDate = date instanceof java.time.LocalDate ? ((java.time.LocalDate) date).plusDays(nights) : null;
            // "27th November" — a friendlier rendering of the SAME check-in
            // date already on this item, not a separate/duplicated value.
            String checkInFormatted = date instanceof java.time.LocalDate
                    ? ordinal(((java.time.LocalDate) date).getDayOfMonth()) + " "
                            + ((java.time.LocalDate) date).getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                    : null;
            List<String> nightOrdinals = new java.util.ArrayList<>();
            if (dayNumber != null) {
                for (int i = 0; i < nights; i++) {
                    nightOrdinals.add(ordinal(dayNumber + i));
                }
            }
            Integer stars = hotel != null ? hotel.getStars() : null;
            // One list entry per star — lets the template print a repeated
            // ★ per point (e.g. "★★★★★" for a 5-star hotel) via a Mustache
            // loop, since Mustache can't repeat a literal N times on its own.
            List<Object> starIcons = new java.util.ArrayList<>();
            if (stars != null) {
                for (int i = 0; i < stars; i++) {
                    starIcons.add(map());
                }
            }
            m.put("hotel", map(
                    "roomCount", item.getHotelDetail().getRoomCount(),
                    "paxPerRoom", item.getHotelDetail().getPaxPerRoom(),
                    "totalPrice", item.getHotelDetail().getTotalPrice(),
                    "mealPlanName", mealPlanNames.get(item.getHotelDetail().getMealPlanId()),
                    "roomTypeName", roomTypeNames.get(item.getHotelDetail().getRoomTypeId()),
                    "stars", stars,
                    "starIcons", starIcons,
                    "photoUrl", hotel != null && hotel.getImages() != null && !hotel.getImages().isEmpty() ? hotel.getImages().get(0) : null,
                    // A library-sourced hotel is still an indicative pick
                    // (agent may substitute an equivalent property at
                    // booking time) — a custom/ad-hoc item names a hotel
                    // the agent typed in directly, so no "/ Similar" caveat
                    // applies.
                    "isSimilarOption", "library".equals(item.getSource()),
                    "checkInTime", hotel != null ? hotel.getCheckInTime() : null,
                    "checkOutTime", hotel != null ? hotel.getCheckOutTime() : null,
                    "cityName", hotel != null && hotel.getLocation() != null ? hotel.getLocation().getCity() : null,
                    "nights", nights,
                    "checkOutDate", checkOutDate,
                    "checkInFormatted", checkInFormatted,
                    // ["1st", "2nd", "3rd"] for a 3-night stay starting on
                    // this item's own day — drives the "[1st][2nd][3rd]
                    // Nights at {city}" badge row.
                    "nightOrdinals", nightOrdinals.stream().map(o -> (Object) map("label", o)).toList(),
                    "adultsWithExtraBed", adultsExtraBed,
                    "childrenWithExtraBed", childrenExtraBed,
                    "childrenNoBed", childrenNoBed,
                    "complimentaryChildCount", complimentaryChild,
                    // Drives whether the template shows an extra-bed line at
                    // all — a stay with none of these set shouldn't print
                    // "0 / 0 / 0 / 0".
                    "hasExtraBedInfo", isPositive(adultsExtraBed) || isPositive(childrenExtraBed)
                            || isPositive(childrenNoBed) || isPositive(complimentaryChild)
            ));
        }
        if (item.getReferenceId() != null && "activity".equals(item.getItemType())) {
            Activity activity = activities.get(item.getReferenceId());
            if (activity != null && activity.getDurationMinutes() != null) {
                m.put("activity", map("durationMinutes", activity.getDurationMinutes()));
            }
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
                    "adultsCount", item.getTransportDetail().getAdultsCount(),
                    "childrenCount", item.getTransportDetail().getChildrenCount(),
                    "infantsCount", item.getTransportDetail().getInfantsCount(),
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
