package com.sss.app.service.payment.impl;

import com.sss.app.dto.payment.PaymentMilestoneCreateRequestDTO;
import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;
import com.sss.app.helper.payment.PaymentMilestoneHelper;
import com.sss.app.mapper.payment.PaymentMilestoneMapper;
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

    @Override
    public PaymentMilestoneResponseDTO create(PaymentMilestoneCreateRequestDTO request) {
        return paymentMilestoneMapper.toResponse(paymentMilestoneHelper.create(request));
    }

    @Override
    public List<PaymentMilestoneResponseDTO> getAllForDeal(UUID dealUid) {
        return paymentMilestoneHelper.getAllForDeal(dealUid).stream().map(paymentMilestoneMapper::toResponse).toList();
    }

    @Override
    public PaymentMilestoneResponseDTO recordPayment(UUID uid, BigDecimal amount) {
        return paymentMilestoneMapper.toResponse(paymentMilestoneHelper.recordPayment(uid, amount));
    }

    @Override
    public void delete(UUID uid) {
        paymentMilestoneHelper.delete(uid);
    }
}
