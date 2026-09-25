package com.sss.app.service.email;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.library.hotel.HotelBookingEmailPreviewDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.itinerary.ItineraryItemHotelDetail;
import com.sss.app.entity.itinerary.ItineraryItemHotelInclusion;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.entity.traveller.TravellerType;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.itinerary.ItineraryItemHotelDetailRepository;
import com.sss.app.repository.itinerary.ItineraryItemHotelInclusionRepository;
import com.sss.app.service.quotationtemplate.QuotationRenderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Sends a BOOKING REQUEST email to the hotel/supplier itself (not the
 * traveller/lead) from the Hotel Detail page's Bookings tab — the recipient
 * is Hotel.email, which the frontend also checks before ever opening the
 * "Send Hotel Booking Email" popup. buildPreview() renders the same subject
 * and body send() actually sends, so the popup shows exactly what goes out;
 * only the subject may be edited by the user before sending, the body is
 * always freshly regenerated server-side.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HotelBookingEmailService {

    private final com.sss.app.service.exchangerate.MoneyFormatter moneyFormatter;

    private static final String EMAIL_BODY_TEMPLATE = "email-templates/hotel-booking-request-email.mustache";
    // Triple-stache: this subject is plain text, not HTML, so tripCode/hotelName
    // must not be HTML-entity-escaped the way Mustache's default {{}} would.
    private static final String SUBJECT_TEMPLATE = "Booking Request – Escape ID: {{{tripCode}}} – {{{hotelName}}}";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final ItineraryItemHotelDetailRepository hotelDetailRepository;
    private final ItineraryItemHotelInclusionRepository hotelInclusionRepository;
    private final OrganizationRepository organizationRepository;
    private final QuotationRenderingService quotationRenderingService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public HotelBookingEmailPreviewDTO buildPreview(Hotel hotel, ItineraryItem item) {
        Map<String, Object> data = buildData(hotel, item);
        String subject = quotationRenderingService.renderInline(SUBJECT_TEMPLATE, data);
        String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
        return new HotelBookingEmailPreviewDTO(hotelEmail(hotel), subject, body);
    }

    @Transactional(readOnly = true)
    public SendEmailResponseDTO send(Hotel hotel, ItineraryItem item, String subjectOverride) {
        String toEmail = hotelEmail(hotel);
        if (toEmail == null) {
            throw new BadRequestException("Hotel email not available.");
        }
        Map<String, Object> data = buildData(hotel, item);
        String subject = subjectOverride != null && !subjectOverride.isBlank()
                ? subjectOverride
                : quotationRenderingService.renderInline(SUBJECT_TEMPLATE, data);
        String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
        emailService.sendHtmlEmail(List.of(toEmail), subject, body);
        return new SendEmailResponseDTO(List.of(toEmail));
    }

    private String hotelEmail(Hotel hotel) {
        return hotel.getEmail() != null && !hotel.getEmail().isBlank() ? hotel.getEmail().trim() : null;
    }

    private Map<String, Object> buildData(Hotel hotel, ItineraryItem item) {
        Escape escape = item.getItinerary().getEscape();
        ItineraryItemHotelDetail detail = hotelDetailRepository.findByItineraryItem_Seqp(item.getSeqp()).orElse(null);
        List<ItineraryItemHotelInclusion> inclusions =
                hotelInclusionRepository.findAllByItineraryItem_SeqpOrderBySeqpAsc(item.getSeqp());
        List<String> serviceNames = inclusions.stream()
                .map(ItineraryItemHotelInclusion::getService)
                .filter(Objects::nonNull)
                .toList();

        Organizations org = organizationRepository.findById(hotel.getOrgId()).orElse(null);
        String orgName = org != null
                ? (org.getDisplayName() != null && !org.getDisplayName().isBlank() ? org.getDisplayName() : org.getRegisteredName())
                : "";

        LocalDate checkIn = escape.getStartDate() != null && item.getDayNumber() != null
                ? escape.getStartDate().plusDays(item.getDayNumber() - 1)
                : null;
        Integer nights = detail != null ? detail.getNights() : null;
        LocalDate checkOut = checkIn != null && nights != null ? checkIn.plusDays(nights) : null;

        long adultCount = escape.getTravellers().stream().filter(t -> TravellerType.ADULT.equals(t.getType())).count();
        long childCount = escape.getTravellers().stream().filter(t -> TravellerType.CHILD.equals(t.getType())).count();
        long infantCount = escape.getTravellers().stream().filter(t -> TravellerType.INFANT.equals(t.getType())).count();

        Map<String, Object> data = new HashMap<>();
        data.put("organizationName", orgName);

        // Escape details
        data.put("tripCode", escape.getTripCode());
        data.put("escapeName", escape.getLead() != null && escape.getLead().getName() != null ? escape.getLead().getName() : "—");
        data.put("escapePoint", escape.getEscapePoints().stream()
                .map(EscapePoint::getName)
                .collect(Collectors.joining(", ")));
        data.put("escapeStartDate", formatDate(escape.getStartDate()));
        data.put("escapeEndDate", formatDate(escape.getEndDate()));

        // Traveler details
        data.put("primaryTravellerName", resolvePrimaryTravellerName(escape));
        data.put("adultCount", adultCount > 0 ? adultCount : null);
        data.put("childCount", childCount > 0 ? childCount : null);
        data.put("infantCount", infantCount > 0 ? infantCount : null);

        // Hotel booking details
        data.put("hotelName", hotel.getName());
        data.put("checkInDate", formatDate(checkIn));
        data.put("checkOutDate", formatDate(checkOut));
        data.put("nights", nights);
        data.put("roomTypeName", detail != null && detail.getRoomType() != null ? detail.getRoomType().getName() : null);
        data.put("roomCount", detail != null ? detail.getRoomCount() : null);
        data.put("mealPlanName", detail != null && detail.getMealPlan() != null ? detail.getMealPlan().getName() : null);
        data.put("paxPerRoom", detail != null ? detail.getPaxPerRoom() : null);
        data.put("services", serviceNames);
        data.put("hasServices", !serviceNames.isEmpty());
        data.put("notes", item.getNotes());

        BigDecimal totalAmount = bookingTotalAmount(detail, inclusions);
        data.put("totalAmountFormatted", totalAmount != null ? moneyFormat().format(totalAmount) : null);

        return data;
    }

    // Mirrors QuoteComputationServiceImpl's hotel-item pricing branch and
    // HotelServiceImpl.bookingTotalAmount — a Dropped stay contributes only
    // its cancellation charge; otherwise stay price (or price × roomCount)
    // plus every inclusion's totalPrice.
    private BigDecimal bookingTotalAmount(ItineraryItemHotelDetail detail, List<ItineraryItemHotelInclusion> inclusions) {
        BigDecimal inclusionsTotal = inclusions.stream()
                .map(ItineraryItemHotelInclusion::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (detail == null) {
            return inclusionsTotal;
        }
        if (BookingStatus.DROP.equals(detail.getStatus())) {
            return detail.getCancellationChargeBase() != null ? detail.getCancellationChargeBase() : BigDecimal.ZERO;
        }
        BigDecimal stayPrice = detail.getTotalPrice() != null
                ? detail.getTotalPrice()
                : (detail.getPrice() != null && detail.getRoomCount() != null
                        ? detail.getPrice().multiply(BigDecimal.valueOf(detail.getRoomCount()))
                        : BigDecimal.ZERO);
        return stayPrice.add(inclusionsTotal);
    }

    private String resolvePrimaryTravellerName(Escape escape) {
        UUID primaryTravellerUid = escape.getPrimaryTravellerUid();
        if (primaryTravellerUid != null && escape.getTravellers() != null) {
            for (Traveller traveller : escape.getTravellers()) {
                if (primaryTravellerUid.equals(traveller.getUid())) {
                    return travellerDisplayName(traveller);
                }
            }
        }
        return escape.getLead() != null && escape.getLead().getName() != null ? escape.getLead().getName() : "Guest";
    }

    private String travellerDisplayName(Traveller traveller) {
        String first = traveller.getFirstName() != null ? traveller.getFirstName().trim() : "";
        String last = traveller.getLastName() != null ? traveller.getLastName().trim() : "";
        String name = (first + " " + last).trim();
        return name.isBlank() ? "Guest" : name;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : null;
    }

    private java.text.NumberFormat moneyFormat() {
        return moneyFormatter.forCaller();
    }
}
