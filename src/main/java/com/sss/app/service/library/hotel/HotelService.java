package com.sss.app.service.library.hotel;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.library.hotel.HotelBookingDTO;
import com.sss.app.dto.library.hotel.HotelBookingEmailPreviewDTO;
import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelPaymentCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelPaymentResponseDTO;
import com.sss.app.dto.library.hotel.HotelPriorityImageRequestDTO;
import com.sss.app.dto.library.hotel.HotelResponseDTO;
import com.sss.app.dto.library.hotel.HotelUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface HotelService {

    HotelResponseDTO create(HotelCreateRequestDTO dto);

    HotelResponseDTO getById(UUID id);

    List<HotelResponseDTO> getAll();

    HotelResponseDTO update(UUID id, HotelUpdateRequestDTO dto);

    HotelResponseDTO setPriorityImage(UUID id, HotelPriorityImageRequestDTO dto);

    void delete(UUID id);

    List<HotelBookingDTO> getBookings(UUID id);

    HotelBookingDTO markBooked(UUID id, UUID itineraryItemUid);

    HotelBookingEmailPreviewDTO getBookingEmailPreview(UUID id, UUID itineraryItemUid);

    HotelBookingEmailPreviewDTO getCancellationEmailPreview(UUID id, UUID itineraryItemUid);

    SendEmailResponseDTO sendBookingEmail(UUID id, UUID itineraryItemUid, String subject);

    SendEmailResponseDTO sendCancellationEmail(UUID id, UUID itineraryItemUid, String subject);

    List<HotelPaymentResponseDTO> getPayments(UUID id);

    HotelPaymentResponseDTO createPayment(UUID id, HotelPaymentCreateRequestDTO dto);
}
