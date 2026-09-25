package com.sss.app.service.payment;

import com.sss.app.dto.payment.PaymentRecordResponseDTO;
import com.sss.app.entity.payment.PaymentRecord;
import com.sss.app.entity.users.User;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.payment.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Turns payment_records into their response DTOs (with the recorder's name resolved). */
@Component
@RequiredArgsConstructor
public class PaymentRecordAssembler {

    private final PaymentRecordRepository paymentRecordRepository;
    private final UserRepository userRepository;

    public List<PaymentRecordResponseDTO> forMilestone(Long milestoneSeqp) {
        return paymentRecordRepository.findAllByMilestone_SeqpOrderByRecordedAtAsc(milestoneSeqp).stream().map(this::toDto).toList();
    }

    public PaymentRecordResponseDTO toDto(PaymentRecord r) {
        PaymentRecordResponseDTO p = new PaymentRecordResponseDTO();
        p.setUid(r.getUid());
        p.setReceivedAmount(r.getReceivedAmount());
        p.setReceivedCurrency(r.getReceivedCurrency());
        p.setFxRate(r.getFxRate());
        p.setAppliedAmountBase(r.getAppliedAmountBase());
        p.setBaseValueReceived(r.getBaseValueReceived());
        p.setFxDifferenceBase(r.getFxDifferenceBase());
        p.setPaymentMethod(r.getPaymentMethod());
        p.setPaymentReference(r.getPaymentReference());
        p.setRecordedAt(r.getRecordedAt());
        p.setVerifiedAt(r.getVerifiedAt());
        if (r.getRecordedBy() != null) {
            userRepository.findById(r.getRecordedBy()).map(User::getName).ifPresent(p::setRecordedByName);
        }
        return p;
    }
}
