package com.sss.app.service.email;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.library.activity.ActivityBookingEmailPreviewDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.library.activity.Activity;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.organizations.Organizations;
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
import java.util.stream.Collectors;

/**
 * Sends a BOOKING CANCELLED email to the activity vendor/supplier itself —
 * the cancellation counterpart of ActivityBookingEmailService, triggered
 * from the same Activity Detail page's Bookings tab once a booking has been
 * Dropped. Mirrors ActivityBookingEmailService's shape exactly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityCancellationEmailService {

    private final com.sss.app.service.exchangerate.MoneyFormatter moneyFormatter;

    private static final String EMAIL_BODY_TEMPLATE = "email-templates/activity-cancellation-email.mustache";
    private static final String SUBJECT_TEMPLATE = "Booking Cancelled – Escape ID: {{{tripCode}}} – {{{activityName}}}";
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
        if (!BookingStatus.DROP.equals(item.getStatus())) {
            throw new BadRequestException("This activity booking hasn't been dropped — nothing to send a cancellation for.");
        }
        Escape escape = item.getItinerary().getEscape();

        Organizations org = organizationRepository.findById(activity.getOrgId()).orElse(null);
        String orgName = org != null
                ? (org.getDisplayName() != null && !org.getDisplayName().isBlank() ? org.getDisplayName() : org.getRegisteredName())
                : "";

        LocalDate activityDate = escape.getStartDate() != null && item.getDayNumber() != null
                ? escape.getStartDate().plusDays(item.getDayNumber() - 1)
                : null;

        Map<String, Object> data = new HashMap<>();
        data.put("organizationName", orgName);

        data.put("tripCode", escape.getTripCode());
        data.put("escapeName", escape.getLead() != null && escape.getLead().getName() != null ? escape.getLead().getName() : "—");
        data.put("escapePoint", escape.getEscapePoints().stream()
                .map(EscapePoint::getName)
                .collect(Collectors.joining(", ")));
        data.put("escapeStartDate", formatDate(escape.getStartDate()));
        data.put("escapeEndDate", formatDate(escape.getEndDate()));

        data.put("activityName", activity.getName());
        data.put("activityDate", formatDate(activityDate));
        data.put("startTime", item.getStartTime() != null ? item.getStartTime().format(TIME_FORMAT) : null);
        data.put("cancellationReason", item.getDroppingReason());

        // Mirrors QuoteComputationServiceImpl's activity-item pricing branch
        // — a Dropped booking bills only its cancellation charge.
        BigDecimal charge = item.getCancellationChargeBase() != null ? item.getCancellationChargeBase() : BigDecimal.ZERO;
        data.put("totalAmountFormatted", moneyFormat().format(charge));

        return data;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : null;
    }

    private java.text.NumberFormat moneyFormat() {
        return moneyFormatter.forCaller();
    }
}
