package com.sss.app.service.library.activity;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.library.activity.ActivityBookingDTO;
import com.sss.app.dto.library.activity.ActivityBookingEmailPreviewDTO;
import com.sss.app.dto.library.activity.ActivityCreateRequestDTO;
import com.sss.app.dto.library.activity.ActivityPaymentCreateRequestDTO;
import com.sss.app.dto.library.activity.ActivityPaymentResponseDTO;
import com.sss.app.dto.library.activity.ActivityResponseDTO;
import com.sss.app.dto.library.activity.ActivityUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface ActivityService {
    ActivityResponseDTO create(ActivityCreateRequestDTO dto);

    ActivityResponseDTO getById(UUID id);

    List<ActivityResponseDTO> getAll();

    ActivityResponseDTO update(UUID id, ActivityUpdateRequestDTO dto);

    void delete(UUID id);

    List<ActivityBookingDTO> getBookings(UUID id);

    ActivityBookingDTO markBooked(UUID id, UUID itineraryItemUid);

    ActivityBookingEmailPreviewDTO getBookingEmailPreview(UUID id, UUID itineraryItemUid);

    SendEmailResponseDTO sendBookingEmail(UUID id, UUID itineraryItemUid, String subject);

    List<ActivityPaymentResponseDTO> getPayments(UUID id);

    ActivityPaymentResponseDTO createPayment(UUID id, ActivityPaymentCreateRequestDTO dto);
}
