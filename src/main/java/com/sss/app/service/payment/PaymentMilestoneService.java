package com.sss.app.service.payment;

import com.sss.app.dto.payment.PaymentMilestoneCreateRequestDTO;
import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentMilestoneService {
    PaymentMilestoneResponseDTO create(PaymentMilestoneCreateRequestDTO request);
    List<PaymentMilestoneResponseDTO> getAllForDeal(UUID dealUid);
    PaymentMilestoneResponseDTO recordPayment(UUID uid, BigDecimal amount, String paymentMethod, String paymentReference);
    PaymentMilestoneResponseDTO verifyPayment(UUID uid);
    void delete(UUID uid);
}
