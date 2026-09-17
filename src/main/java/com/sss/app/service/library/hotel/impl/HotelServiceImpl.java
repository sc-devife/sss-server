package com.sss.app.service.library.hotel.impl;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.itinerary.HotelDetailDTO;
import com.sss.app.dto.itinerary.ItineraryItemUpdateRequestDTO;
import com.sss.app.dto.library.hotel.HotelBookingDTO;
import com.sss.app.dto.library.hotel.HotelBookingEmailPreviewDTO;
import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelPaymentCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelPaymentResponseDTO;
import com.sss.app.dto.library.hotel.HotelPriorityImageRequestDTO;
import com.sss.app.dto.library.hotel.HotelResponseDTO;
import com.sss.app.dto.library.hotel.HotelRoomTypeResponseDTO;
import com.sss.app.dto.library.hotel.HotelUpdateRequestDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.itinerary.BookingStatus;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.itinerary.ItineraryItemHotelDetail;
import com.sss.app.entity.itinerary.ItineraryItemHotelInclusion;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.entity.library.hotel.HotelPayment;
import com.sss.app.entity.library.hotel.HotelRoomType;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.helper.itinerary.ItineraryItemHelper;
import com.sss.app.helper.library.hotel.HotelHelper;
import com.sss.app.mapper.library.hotel.HotelMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.itinerary.ItineraryItemHotelDetailRepository;
import com.sss.app.repository.itinerary.ItineraryItemHotelInclusionRepository;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
import com.sss.app.repository.library.hotel.HotelPaymentRepository;
import com.sss.app.repository.library.hotel.HotelRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.email.HotelBookingEmailService;
import com.sss.app.service.email.HotelCancellationEmailService;
import com.sss.app.service.files.CloudinaryService;
import com.sss.app.service.itinerary.ItineraryItemService;
import com.sss.app.service.library.hotel.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final HotelHelper hotelHelper;
    private final OrgAccessGuard orgAccessGuard;
    private final CloudinaryService cloudinaryService;
    private final ItineraryItemRepository itineraryItemRepository;
    private final ItineraryItemHotelDetailRepository hotelDetailRepository;
    private final ItineraryItemHotelInclusionRepository hotelInclusionRepository;
    private final HotelBookingEmailService hotelBookingEmailService;
    private final HotelCancellationEmailService hotelCancellationEmailService;
    private final ItineraryItemHelper itineraryItemHelper;
    private final ItineraryItemService itineraryItemService;
    private final HotelPaymentRepository hotelPaymentRepository;
    private final EscapeRepository escapeRepository;
    private final AuditLogService auditLogService;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public HotelResponseDTO create(HotelCreateRequestDTO dto) {
        Hotel hotel = hotelMapper.toEntityCreate(dto);
        hotel.setOrgId(currentUser().getOrgId());

        // Resolve & wire relations (location is required, rest are optional)
        hotelHelper.applyRelations(
                hotel,
                dto.getLocationId(),
                dto.getEscapePointId(),
                dto.getEscapePointIds(),
                dto.getMealPlanIds(),
                dto.getRoomTypePricing(),
                dto.getServiceIds()
        );

        resolvePriorityImage(hotel);
        Hotel saved = hotelRepository.save(hotel);
        return toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponseDTO getById(UUID id) {
        return toResponseDto(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDTO> getAll() {
        return hotelRepository.findAllByOrgIdAndDeletedAtIsNull(currentUser().getOrgId())
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public HotelResponseDTO update(UUID id, HotelUpdateRequestDTO dto) {
        Hotel hotel = findEntityById(id);
        List<String> previousImages = hotel.getImages() == null ? null : new ArrayList<>(hotel.getImages());

        // Update scalar fields (name, stars, checkIn/Out, childAge, isActive)
        hotelMapper.updateEntityFromDto(dto, hotel);

        // Update relations only if provided in the request (partial update friendly)
        hotelHelper.applyRelations(
                hotel,
                dto.getLocationId(),
                dto.getEscapePointId(),
                dto.getEscapePointIds(),
                dto.getMealPlanIds(),
                dto.getRoomTypePricing(),
                dto.getServiceIds()
        );

        resolvePriorityImage(hotel);
        Hotel saved = hotelRepository.save(hotel);
        cloudinaryService.deleteRemoved(previousImages, saved.getImages());
        return toResponseDto(saved);
    }

    // Keeps priority_image consistent whenever the gallery changes: a single
    // image is always its own priority, and if no priority is stored yet or
    // the previously-chosen one was just removed, fall back to the first
    // remaining image. Mirrors EscapePointsHelper.resolvePriorityImage.
    private void resolvePriorityImage(Hotel hotel) {
        List<String> images = hotel.getImages();
        if (images == null || images.isEmpty()) {
            hotel.setPriorityImage(null);
            return;
        }
        if (images.size() == 1 || hotel.getPriorityImage() == null || !images.contains(hotel.getPriorityImage())) {
            hotel.setPriorityImage(images.get(0));
        }
    }

    @Override
    public HotelResponseDTO setPriorityImage(UUID id, HotelPriorityImageRequestDTO dto) {
        Hotel hotel = findEntityById(id);
        List<String> images = hotel.getImages();
        if (images == null || !images.contains(dto.getImageUrl())) {
            throw new IllegalArgumentException("imageUrl must be one of this Hotel's existing images");
        }
        hotel.setPriorityImage(dto.getImageUrl());
        Hotel saved = hotelRepository.save(hotel);
        return toResponseDto(saved);
    }

    // hotelMapper.toResponse() leaves `roomTypes` unset (see HotelMapper) since
    // HotelRoomType's fields don't name-match HotelRoomTypeResponseDTO for
    // MapStruct to auto-map — filled in here from the join rows instead.
    private HotelResponseDTO toResponseDto(Hotel hotel) {
        HotelResponseDTO dto = hotelMapper.toResponse(hotel);
        dto.setRoomTypes(hotel.getRoomTypes().stream()
                .map(hrt -> new HotelRoomTypeResponseDTO(
                        hrt.getRoomType().getUid(),
                        hrt.getRoomType().getName(),
                        hrt.getRoomType().getDescription(),
                        hrt.getPrice()))
                .toList());
        return dto;
    }

    @Override
    public void delete(UUID id) {
        Hotel hotel = findEntityById(id);
        hotel.setDeletedAt(java.time.LocalDateTime.now());
        hotel.setStatus("archived");
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelBookingDTO> getBookings(UUID id) {
        Hotel hotel = findEntityById(id);
        return itineraryItemRepository.findAllHotelBookings(hotel.getUid())
                .stream()
                .map(this::toBookingDTO)
                .toList();
    }

    // Goes through the exact same PUT-the-whole-item path the Escape Detail
    // page's hotel-status dropdown uses (ItineraryItemHelper.update ->
    // saveHotelDetail), instead of flipping the status column directly —
    // that's the only way to also get its audit-log entry and the itinerary's
    // quote recompute, so both places stay behaviorally identical.
    @Override
    public HotelBookingDTO markBooked(UUID id, UUID itineraryItemUid) {
        Hotel hotel = findEntityById(id);
        ItineraryItem item = findBookingItem(hotel, itineraryItemUid);

        HotelDetailDTO hotelDetail = itineraryItemHelper.getHotelDetail(item);
        if (hotelDetail == null) {
            throw new BadRequestException("This booking has no hotel stay details to update yet.");
        }
        hotelDetail.setStatus(BookingStatus.BOOKED);

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
        request.setHotelDetail(hotelDetail);

        itineraryItemService.update(item.getUid(), request);
        return toBookingDTO(item);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelBookingEmailPreviewDTO getBookingEmailPreview(UUID id, UUID itineraryItemUid) {
        Hotel hotel = findEntityById(id);
        ItineraryItem item = findBookingItem(hotel, itineraryItemUid);
        return hotelBookingEmailService.buildPreview(hotel, item);
    }

    @Override
    public SendEmailResponseDTO sendBookingEmail(UUID id, UUID itineraryItemUid, String subject) {
        Hotel hotel = findEntityById(id);
        ItineraryItem item = findBookingItem(hotel, itineraryItemUid);
        return hotelBookingEmailService.send(hotel, item, subject);
    }

    @Override
    public HotelBookingEmailPreviewDTO getCancellationEmailPreview(UUID id, UUID itineraryItemUid) {
        Hotel hotel = findEntityById(id);
        ItineraryItem item = findBookingItem(hotel, itineraryItemUid);
        return hotelCancellationEmailService.buildPreview(hotel, item);
    }

    @Override
    public SendEmailResponseDTO sendCancellationEmail(UUID id, UUID itineraryItemUid, String subject) {
        Hotel hotel = findEntityById(id);
        ItineraryItem item = findBookingItem(hotel, itineraryItemUid);
        return hotelCancellationEmailService.send(hotel, item, subject);
    }

    private ItineraryItem findBookingItem(Hotel hotel, UUID itineraryItemUid) {
        ItineraryItem item = itineraryItemRepository.findByUid(itineraryItemUid)
                .orElseThrow(() -> new ResourceNotFoundException("ItineraryItem", itineraryItemUid));
        if (!hotel.getUid().equals(item.getReferenceId())) {
            throw new IllegalArgumentException("Booking does not belong to this hotel");
        }
        return item;
    }

    private HotelBookingDTO toBookingDTO(ItineraryItem item) {
        Itinerary itinerary = item.getItinerary();
        Escape escape = itinerary.getEscape();
        ItineraryItemHotelDetail detail = hotelDetailRepository.findByItineraryItem_Seqp(item.getSeqp()).orElse(null);
        List<ItineraryItemHotelInclusion> inclusions =
                hotelInclusionRepository.findAllByItineraryItem_SeqpOrderBySeqpAsc(item.getSeqp());
        List<String> serviceNames = inclusions.stream()
                .map(ItineraryItemHotelInclusion::getService)
                .filter(Objects::nonNull)
                .toList();
        return new HotelBookingDTO(
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
                detail != null ? detail.getStatus() : null,
                detail != null && detail.getMealPlan() != null ? detail.getMealPlan().getName() : null,
                serviceNames,
                bookingTotalAmount(detail, inclusions)
        );
    }

    // Mirrors QuoteComputationServiceImpl's hotel-item pricing branch: a
    // Dropped stay contributes only its cancellation charge, otherwise the
    // stay price (or price × roomCount) plus every inclusion's totalPrice —
    // so this figure always matches what Quotation actually bills.
    private BigDecimal bookingTotalAmount(ItineraryItemHotelDetail detail, List<ItineraryItemHotelInclusion> inclusions) {
        BigDecimal inclusionsTotal = inclusions.stream()
                .map(ItineraryItemHotelInclusion::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (detail == null) {
            return inclusionsTotal;
        }
        if (BookingStatus.DROP.equals(detail.getStatus())) {
            return detail.getCancellationChargeInr() != null ? detail.getCancellationChargeInr() : BigDecimal.ZERO;
        }
        BigDecimal stayPrice = detail.getTotalPrice() != null
                ? detail.getTotalPrice()
                : (detail.getPrice() != null && detail.getRoomCount() != null
                        ? detail.getPrice().multiply(BigDecimal.valueOf(detail.getRoomCount()))
                        : BigDecimal.ZERO);
        return stayPrice.add(inclusionsTotal);
    }

    private Hotel findEntityById(UUID id) {
        Hotel hotel = hotelRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));
        orgAccessGuard.requireAccessToOrg(hotel.getOrgId());
        return hotel;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelPaymentResponseDTO> getPayments(UUID id) {
        Hotel hotel = findEntityById(id);
        return hotelPaymentRepository.findAllByHotelUid(hotel.getUid())
                .stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Override
    public HotelPaymentResponseDTO createPayment(UUID id, HotelPaymentCreateRequestDTO dto) {
        Hotel hotel = findEntityById(id);
        Escape escape = escapeRepository.findByUid(dto.getEscapeUid())
                .orElseThrow(() -> new ResourceNotFoundException("Escape", dto.getEscapeUid()));
        orgAccessGuard.requireAccessToOrg(escape.getOrgId());

        HotelPayment payment = HotelPayment.builder()
                .orgId(hotel.getOrgId())
                .hotel(hotel)
                .escape(escape)
                .transactionId(dto.getTransactionId())
                .paymentMethod(dto.getPaymentMethod())
                .amount(dto.getAmount())
                .paidBy(dto.getPaidBy())
                .paymentDate(dto.getPaymentDate())
                .notes(dto.getNotes())
                .build();
        HotelPayment saved = hotelPaymentRepository.save(payment);

        auditLogService.record("Hotel", hotel.getSeqp(), "HOTEL_PAYMENT_RECORDED", null,
                dto.getAmount() + " via " + dto.getPaymentMethod() + " for " + escape.getTripCode());

        return toPaymentResponse(saved);
    }

    private HotelPaymentResponseDTO toPaymentResponse(HotelPayment payment) {
        return new HotelPaymentResponseDTO(
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
}
