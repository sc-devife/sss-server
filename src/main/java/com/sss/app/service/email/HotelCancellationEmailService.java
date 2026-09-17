package com.sss.app.service.email;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.library.hotel.HotelBookingEmailPreviewDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.itinerary.ItineraryItemHotelDetail;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.itinerary.ItineraryItemHotelDetailRepository;
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
 * Sends a BOOKING CANCELLED email to the hotel/supplier itself — the
 * cancellation counterpart of HotelBookingEmailService, triggered from the
 * same Hotel Detail page's Bookings tab once a booking has been Dropped.
 * Mirrors HotelBookingEmailService's shape exactly (buildPreview/send,
 * subject-only editable, server-regenerated body) and reuses its own
 * response DTO, since the popup shape is identical — only the recipient
 * content (subject, template, reason/charge) differs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HotelCancellationEmailService {

    private static final String EMAIL_BODY_TEMPLATE = "email-templates/hotel-cancellation-email.mustache";
    private static final String SUBJECT_TEMPLATE = "Booking Cancelled – Escape ID: {{{tripCode}}} – {{{hotelName}}}";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final ItineraryItemHotelDetailRepository hotelDetailRepository;
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
        if (detail == null || !BookingStatus.DROP.equals(detail.getStatus())) {
            throw new BadRequestException("This hotel booking hasn't been dropped — nothing to send a cancellation for.");
        }

        Organizations org = organizationRepository.findById(hotel.getOrgId()).orElse(null);
        String orgName = org != null
                ? (org.getDisplayName() != null && !org.getDisplayName().isBlank() ? org.getDisplayName() : org.getRegisteredName())
                : "";

        LocalDate checkIn = escape.getStartDate() != null && item.getDayNumber() != null
                ? escape.getStartDate().plusDays(item.getDayNumber() - 1)
                : null;
        Integer nights = detail.getNights();
        LocalDate checkOut = checkIn != null && nights != null ? checkIn.plusDays(nights) : null;

        Map<String, Object> data = new HashMap<>();
        data.put("organizationName", orgName);

        data.put("tripCode", escape.getTripCode());
        data.put("escapeName", escape.getLead() != null && escape.getLead().getName() != null ? escape.getLead().getName() : "—");
        data.put("escapePoint", escape.getEscapePoints().stream()
                .map(EscapePoint::getName)
                .collect(Collectors.joining(", ")));
        data.put("escapeStartDate", formatDate(escape.getStartDate()));
        data.put("escapeEndDate", formatDate(escape.getEndDate()));

        data.put("hotelName", hotel.getName());
        data.put("checkInDate", formatDate(checkIn));
        data.put("checkOutDate", formatDate(checkOut));
        data.put("roomTypeName", detail.getRoomType() != null ? detail.getRoomType().getName() : null);
        data.put("cancellationReason", detail.getDroppingReason());

        // Mirrors QuoteComputationServiceImpl's hotel-item pricing branch — a
        // Dropped stay bills only its cancellation charge, never the
        // original nominal price.
        BigDecimal charge = detail.getCancellationChargeInr() != null ? detail.getCancellationChargeInr() : BigDecimal.ZERO;
        data.put("totalAmountFormatted", inrWholeFormat().format(charge));

        return data;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : null;
    }

    private NumberFormat inrWholeFormat() {
        NumberFormat format = NumberFormat.getInstance(new Locale("en", "IN"));
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format;
    }
}
