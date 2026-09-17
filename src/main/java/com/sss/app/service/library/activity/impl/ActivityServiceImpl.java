package com.sss.app.service.library.activity.impl;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.itinerary.ItineraryItemUpdateRequestDTO;
import com.sss.app.dto.library.activity.ActivityBookingDTO;
import com.sss.app.dto.library.activity.ActivityBookingEmailPreviewDTO;
import com.sss.app.dto.library.activity.ActivityCreateRequestDTO;
import com.sss.app.dto.library.activity.ActivityPaymentCreateRequestDTO;
import com.sss.app.dto.library.activity.ActivityPaymentResponseDTO;
import com.sss.app.dto.library.activity.ActivityResponseDTO;
import com.sss.app.dto.library.activity.ActivityUpdateRequestDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.library.activity.Activity;
import com.sss.app.entity.library.activity.ActivityPayment;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.helper.library.activity.ActivityHelper;
import com.sss.app.mapper.library.activity.ActivityMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
import com.sss.app.repository.library.activity.ActivityPaymentRepository;
import com.sss.app.repository.library.activity.ActivityRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.email.ActivityBookingEmailService;
import com.sss.app.service.email.ActivityCancellationEmailService;
import com.sss.app.service.files.CloudinaryService;
import com.sss.app.service.itinerary.ItineraryItemService;
import com.sss.app.service.library.activity.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final ActivityHelper activityHelper;
    private final OrgAccessGuard orgAccessGuard;
    private final CloudinaryService cloudinaryService;
    private final ItineraryItemRepository itineraryItemRepository;
    private final ItineraryItemService itineraryItemService;
    private final ActivityBookingEmailService activityBookingEmailService;
    private final ActivityCancellationEmailService activityCancellationEmailService;
    private final ActivityPaymentRepository activityPaymentRepository;
    private final EscapeRepository escapeRepository;
    private final AuditLogService auditLogService;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public ActivityResponseDTO create(ActivityCreateRequestDTO dto) {
        Activity activity = activityMapper.toEntityCreate(dto);
        activity.setOrgId(currentUser().getOrgId());
        if (dto.getEscapePointId() != null) {
            activity.setEscapePoint(activityHelper.resolveEscapePoint(dto.getEscapePointId()));
        }
        Activity saved = activityRepository.save(activity);
        return activityMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponseDTO getById(UUID id) {
        return activityMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getAll() {
        return activityRepository.findAllByOrgIdAndDeletedAtIsNull(currentUser().getOrgId())
                .stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Override
    public ActivityResponseDTO update(UUID id, ActivityUpdateRequestDTO dto) {
        Activity activity = findEntityById(id);
        List<String> previousImages = activity.getImages() == null ? null : new ArrayList<>(activity.getImages());
        activityMapper.updateEntityFromDto(dto, activity);
        if (dto.getEscapePointId() != null) {
            activity.setEscapePoint(activityHelper.resolveEscapePoint(dto.getEscapePointId()));
        }
        Activity saved = activityRepository.save(activity);
        cloudinaryService.deleteRemoved(previousImages, saved.getImages());
        return activityMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Activity activity = findEntityById(id);
        activity.setDeletedAt(LocalDateTime.now());
        activity.setStatus("archived");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityBookingDTO> getBookings(UUID id) {
        Activity activity = findEntityById(id);
        return itineraryItemRepository.findAllActivityBookings(activity.getUid())
                .stream()
                .map(this::toBookingDTO)
                .toList();
    }

    // Simpler than Hotel's markBooked: an activity item has no separate
    // detail table, so status lives directly on ItineraryItem and the
    // existing update() path (same one the Escape Detail page's own status
    // control uses) already writes it — audit log + quote recompute happen
    // automatically inside ItineraryItemHelper.update().
    @Override
    public ActivityBookingDTO markBooked(UUID id, UUID itineraryItemUid) {
        Activity activity = findEntityById(id);
        ItineraryItem item = findBookingItem(activity, itineraryItemUid);

        ItineraryItemUpdateRequestDTO request = new ItineraryItemUpdateRequestDTO();
        request.setDayNumber(item.getDayNumber());
        request.setItemType(item.getItemType());
        request.setReferenceId(item.getReferenceId());
        request.setTitle(item.getTitle());
        request.setStartTime(item.getStartTime());
        request.setNotes(item.getNotes());
        request.setLongDescription(item.getLongDescription());
        request.setPrice(item.getPrice());
        request.setTravelersCount(item.getTravelersCount());
        request.setStatus(BookingStatus.BOOKED);

        itineraryItemService.update(item.getUid(), request);
        return toBookingDTO(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityBookingEmailPreviewDTO getBookingEmailPreview(UUID id, UUID itineraryItemUid) {
        Activity activity = findEntityById(id);
        ItineraryItem item = findBookingItem(activity, itineraryItemUid);
        return activityBookingEmailService.buildPreview(activity, item);
    }

    @Override
    public SendEmailResponseDTO sendBookingEmail(UUID id, UUID itineraryItemUid, String subject) {
        Activity activity = findEntityById(id);
        ItineraryItem item = findBookingItem(activity, itineraryItemUid);
        return activityBookingEmailService.send(activity, item, subject);
    }

    @Override
    public ActivityBookingEmailPreviewDTO getCancellationEmailPreview(UUID id, UUID itineraryItemUid) {
        Activity activity = findEntityById(id);
        ItineraryItem item = findBookingItem(activity, itineraryItemUid);
        return activityCancellationEmailService.buildPreview(activity, item);
    }

    @Override
    public SendEmailResponseDTO sendCancellationEmail(UUID id, UUID itineraryItemUid, String subject) {
        Activity activity = findEntityById(id);
        ItineraryItem item = findBookingItem(activity, itineraryItemUid);
        return activityCancellationEmailService.send(activity, item, subject);
    }

    private ItineraryItem findBookingItem(Activity activity, UUID itineraryItemUid) {
        ItineraryItem item = itineraryItemRepository.findByUid(itineraryItemUid)
                .orElseThrow(() -> new ResourceNotFoundException("ItineraryItem", itineraryItemUid));
        if (!activity.getUid().equals(item.getReferenceId())) {
            throw new IllegalArgumentException("Booking does not belong to this activity");
        }
        return item;
    }

    private ActivityBookingDTO toBookingDTO(ItineraryItem item) {
        Itinerary itinerary = item.getItinerary();
        Escape escape = itinerary.getEscape();
        return new ActivityBookingDTO(
                item.getUid(),
                escape.getUid(),
                escape.getTripCode(),
                escape.getStatus(),
                escape.getStartDate(),
                escape.getEndDate(),
                escape.getLead() != null ? escape.getLead().getName() : null,
                item.getDayNumber(),
                item.getStartTime(),
                item.getNotes(),
                item.getStatus(),
                bookingTotalAmount(item)
        );
    }

    // Mirrors QuoteComputationServiceImpl's activity-item pricing branch.
    private BigDecimal bookingTotalAmount(ItineraryItem item) {
        if (BookingStatus.DROP.equals(item.getStatus())) {
            return item.getCancellationChargeInr() != null ? item.getCancellationChargeInr() : BigDecimal.ZERO;
        }
        if (item.getPrice() == null) {
            return null;
        }
        int travellers = item.getTravelersCount() != null && item.getTravelersCount() > 0 ? item.getTravelersCount() : 1;
        return item.getPrice().multiply(BigDecimal.valueOf(travellers));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityPaymentResponseDTO> getPayments(UUID id) {
        Activity activity = findEntityById(id);
        return activityPaymentRepository.findAllByActivityUid(activity.getUid())
                .stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Override
    public ActivityPaymentResponseDTO createPayment(UUID id, ActivityPaymentCreateRequestDTO dto) {
        Activity activity = findEntityById(id);
        Escape escape = escapeRepository.findByUid(dto.getEscapeUid())
                .orElseThrow(() -> new ResourceNotFoundException("Escape", dto.getEscapeUid()));
        orgAccessGuard.requireAccessToOrg(escape.getOrgId());

        ActivityPayment payment = ActivityPayment.builder()
                .orgId(activity.getOrgId())
                .activity(activity)
                .escape(escape)
                .transactionId(dto.getTransactionId())
                .paymentMethod(dto.getPaymentMethod())
                .amount(dto.getAmount())
                .paidBy(dto.getPaidBy())
                .paymentDate(dto.getPaymentDate())
                .notes(dto.getNotes())
                .build();
        ActivityPayment saved = activityPaymentRepository.save(payment);

        auditLogService.record("Activity", activity.getSeqp(), "ACTIVITY_PAYMENT_RECORDED", null,
                dto.getAmount() + " via " + dto.getPaymentMethod() + " for " + escape.getTripCode());

        return toPaymentResponse(saved);
    }

    private ActivityPaymentResponseDTO toPaymentResponse(ActivityPayment payment) {
        return new ActivityPaymentResponseDTO(
                payment.getUid(),
                payment.getEscape().getUid(),
                payment.getEscape().getTripCode(),
                payment.getTransactionId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getPaidBy(),
                payment.getPaymentDate(),
                payment.getNotes(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }

    private Activity findEntityById(UUID id) {
        Activity activity = activityRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
        orgAccessGuard.requireAccessToOrg(activity.getOrgId());
        return activity;
    }
}
