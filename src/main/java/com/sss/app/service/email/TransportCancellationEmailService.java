package com.sss.app.service.email;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.library.transport.TransportCancellationEmailPreviewDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.itinerary.ItineraryItemTransportDetail;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.transport.Transport;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.itinerary.ItineraryItemTransportDetailRepository;
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
 * Sends a BOOKING CANCELLED email to the transport vendor itself — the
 * Transport-side counterpart of HotelCancellationEmailService/
 * ActivityCancellationEmailService. Unlike those two, there's no existing
 * Transport BOOKING REQUEST email to mirror (Transport has no library-entity
 * detail page today), so this is new end to end but follows the exact same
 * architecture: buildPreview()/send() pair, mustache template, subject-only
 * editable, EmailService for delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransportCancellationEmailService {

    private final com.sss.app.service.exchangerate.MoneyFormatter moneyFormatter;

    private static final String EMAIL_BODY_TEMPLATE = "email-templates/transport-cancellation-email.mustache";
    private static final String SUBJECT_TEMPLATE = "Booking Cancelled – Escape ID: {{{tripCode}}} – {{{transportName}}}";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final ItineraryItemTransportDetailRepository transportDetailRepository;
    private final OrganizationRepository organizationRepository;
    private final QuotationRenderingService quotationRenderingService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public TransportCancellationEmailPreviewDTO buildPreview(Transport transport, ItineraryItem item) {
        Map<String, Object> data = buildData(transport, item);
        String subject = quotationRenderingService.renderInline(SUBJECT_TEMPLATE, data);
        String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
        return new TransportCancellationEmailPreviewDTO(transportEmail(transport), subject, body);
    }

    @Transactional(readOnly = true)
    public SendEmailResponseDTO send(Transport transport, ItineraryItem item, String subjectOverride) {
        String toEmail = transportEmail(transport);
        if (toEmail == null) {
            throw new BadRequestException("Transport contact email not available.");
        }
        Map<String, Object> data = buildData(transport, item);
        String subject = subjectOverride != null && !subjectOverride.isBlank()
                ? subjectOverride
                : quotationRenderingService.renderInline(SUBJECT_TEMPLATE, data);
        String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
        emailService.sendHtmlEmail(List.of(toEmail), subject, body);
        return new SendEmailResponseDTO(List.of(toEmail));
    }

    private String transportEmail(Transport transport) {
        return transport.getContactEmail() != null && !transport.getContactEmail().isBlank()
                ? transport.getContactEmail().trim() : null;
    }

    private Map<String, Object> buildData(Transport transport, ItineraryItem item) {
        if (!BookingStatus.DROP.equals(item.getStatus())) {
            throw new BadRequestException("This transport booking hasn't been dropped — nothing to send a cancellation for.");
        }
        Escape escape = item.getItinerary().getEscape();
        ItineraryItemTransportDetail detail = transportDetailRepository.findByItineraryItem_Seqp(item.getSeqp()).orElse(null);

        Organizations org = organizationRepository.findById(transport.getOrgId()).orElse(null);
        String orgName = org != null
                ? (org.getDisplayName() != null && !org.getDisplayName().isBlank() ? org.getDisplayName() : org.getRegisteredName())
                : "";

        LocalDate transportDate = escape.getStartDate() != null && item.getDayNumber() != null
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

        String modeCode = detail != null && detail.getModeCode() != null ? detail.getModeCode() : transport.getModeCode();
        String vehicleTypeCode = detail != null && detail.getVehicleTypeCode() != null ? detail.getVehicleTypeCode() : transport.getVehicleTypeCode();
        data.put("transportName", modeCode + (vehicleTypeCode != null && !vehicleTypeCode.isBlank() ? " — " + vehicleTypeCode : ""));
        data.put("transportDate", formatDate(transportDate));
        data.put("startTime", item.getStartTime() != null ? item.getStartTime().format(TIME_FORMAT) : null);
        data.put("cancellationReason", item.getDroppingReason());

        // Mirrors QuoteComputationServiceImpl's transport-item pricing
        // branch — a Dropped booking bills only its cancellation charge.
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
