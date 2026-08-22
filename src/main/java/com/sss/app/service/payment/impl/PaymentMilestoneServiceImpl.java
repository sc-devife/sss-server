package com.sss.app.service.payment.impl;

import com.sss.app.dto.payment.PaymentMilestoneCreateRequestDTO;
import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;
import com.sss.app.entity.payment.PaymentMilestone;
import com.sss.app.entity.users.User;
import com.sss.app.helper.payment.PaymentMilestoneHelper;
import com.sss.app.mapper.payment.PaymentMilestoneMapper;
import com.sss.app.repository.UserRepository;
import com.sss.app.service.payment.PaymentMilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMilestoneServiceImpl implements PaymentMilestoneService {

    private final PaymentMilestoneHelper paymentMilestoneHelper;
    private final PaymentMilestoneMapper paymentMilestoneMapper;
    private final UserRepository userRepository;

    @Override
    public PaymentMilestoneResponseDTO create(PaymentMilestoneCreateRequestDTO request) {
        return toResponse(paymentMilestoneHelper.create(request));
    }

    @Override
    public List<PaymentMilestoneResponseDTO> getAllForDeal(UUID dealUid) {
        return paymentMilestoneHelper.getAllForDeal(dealUid).stream().map(this::toResponse).toList();
    }

    @Override
    public PaymentMilestoneResponseDTO recordPayment(UUID uid, BigDecimal amount) {
        return toResponse(paymentMilestoneHelper.recordPayment(uid, amount));
    }

    @Override
    public PaymentMilestoneResponseDTO verifyPayment(UUID uid) {
        return toResponse(paymentMilestoneHelper.verifyPayment(uid));
    }

    @Override
    public void delete(UUID uid) {
        paymentMilestoneHelper.delete(uid);
    }

    // Wraps the MapStruct mapping to resolve markedPaidBy -> a display name,
    // same reasoning as QuoteServiceImpl.toResponse().
    private PaymentMilestoneResponseDTO toResponse(PaymentMilestone entity) {
        PaymentMilestoneResponseDTO dto = paymentMilestoneMapper.toResponse(entity);
        if (entity.getMarkedPaidBy() != null) {
            userRepository.findById(entity.getMarkedPaidBy()).map(User::getName).ifPresent(dto::setMarkedPaidByName);
        }
        return dto;
    }
}
