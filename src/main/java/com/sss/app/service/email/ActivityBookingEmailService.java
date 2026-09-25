package com.sss.app.service.email;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.library.activity.ActivityBookingEmailPreviewDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.library.activity.Activity;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.entity.traveller.TravellerType;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.OrganizationRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Sends a BOOKING REQUEST email to the activity vendor/supplier itself (not
 * the traveller/lead) from the Activity Detail page's Bookings tab — the
 * Activity-side counterpart of HotelBookingEmailService. The recipient is
 * Activity.email, which the frontend also checks before ever opening the
 * "Send Activity Booking Email" popup. buildPreview() renders the same
 * subject and body send() actually sends, so the popup shows exactly what
 * goes out; only the subject may be edited by the user before sending, the
 * body is always freshly regenerated server-side.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityBookingEmailService {

    private final com.sss.app.service.exchangerate.MoneyFormatter moneyFormatter;

    private static final String EMAIL_BODY_TEMPLATE = "email-templates/activity-booking-request-email.mustache";
    // Triple-stache: this subject is plain text, not HTML, so tripCode/activityName
    // must not be HTML-entity-escaped the way Mustache's default {{}} would.
    private static final String SUBJECT_TEMPLATE = "Booking Request – Escape ID: {{{tripCode}}} – {{{activityName}}}";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final OrganizationRepository organizationRepository;
    private final QuotationRenderingService quotationRenderingService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public ActivityBookingEmailPreviewDTO buildPreview(Activity activity, ItineraryItem item) {
        Map<String, Object> data = buildData(activity, item);
        String subject = quotationRenderingService.renderInline(SUBJECT_TEMPLATE, data);
        String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
        return new ActivityBookingEmailPreviewDTO(activityEmail(activity), subject, body);
    }

    @Transactional(readOnly = true)
    public SendEmailResponseDTO send(Activity activity, ItineraryItem item, String subjectOverride) {
        String toEmail = activityEmail(activity);
        if (toEmail == null) {
            throw new BadRequestException("Activity email not available.");
        }
        Map<String, Object> data = buildData(activity, item);
        String subject = subjectOverride != null && !subjectOverride.isBlank()
                ? subjectOverride
                : quotationRenderingService.renderInline(SUBJECT_TEMPLATE, data);
        String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
        emailService.sendHtmlEmail(List.of(toEmail), subject, body);
        return new SendEmailResponseDTO(List.of(toEmail));
    }

    private String activityEmail(Activity activity) {
        return activity.getEmail() != null && !activity.getEmail().isBlank() ? activity.getEmail().trim() : null;
    }

    private Map<String, Object> buildData(Activity activity, ItineraryItem item) {
        Escape escape = item.getItinerary().getEscape();

        Organizations org = organizationRepository.findById(activity.getOrgId()).orElse(null);
        String orgName = org != null
                ? (org.getDisplayName() != null && !org.getDisplayName().isBlank() ? org.getDisplayName() : org.getRegisteredName())
                : "";

        LocalDate activityDate = escape.getStartDate() != null && item.getDayNumber() != null
                ? escape.getStartDate().plusDays(item.getDayNumber() - 1)
                : null;

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

        // Activity booking details
        data.put("activityName", activity.getName());
        data.put("activityDate", formatDate(activityDate));
        data.put("startTime", item.getStartTime() != null ? item.getStartTime().format(TIME_FORMAT) : null);
        data.put("durationMinutes", activity.getDurationMinutes());
        data.put("travelersCount", item.getTravelersCount());
        data.put("notes", item.getNotes());

        BigDecimal totalAmount = bookingTotalAmount(item);
        data.put("totalAmountFormatted", totalAmount != null ? moneyFormat().format(totalAmount) : null);

        return data;
    }

    // Mirrors QuoteComputationServiceImpl's activity-item pricing branch and
    // ActivityServiceImpl.bookingTotalAmount — a Dropped booking contributes
    // only its cancellation charge; otherwise price × travelersCount.
    private BigDecimal bookingTotalAmount(ItineraryItem item) {
        if (BookingStatus.DROP.equals(item.getStatus())) {
            return item.getCancellationChargeBase() != null ? item.getCancellationChargeBase() : BigDecimal.ZERO;
        }
        if (item.getPrice() == null) {
            return null;
        }
        int travellers = item.getTravelersCount() != null && item.getTravelersCount() > 0 ? item.getTravelersCount() : 1;
        return item.getPrice().multiply(BigDecimal.valueOf(travellers));
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
