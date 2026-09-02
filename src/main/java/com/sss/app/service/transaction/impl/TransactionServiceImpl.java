package com.sss.app.service.transaction.impl;

import com.sss.app.dto.transaction.IncomingTransactionResponseDTO;
import com.sss.app.entity.payment.PaymentMilestone;
import com.sss.app.entity.users.User;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.payment.PaymentMilestoneRepository;
import com.sss.app.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final PaymentMilestoneRepository paymentMilestoneRepository;
    private final UserRepository userRepository;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public List<IncomingTransactionResponseDTO> getIncomingTransactions() {
        Long orgId = currentUser().getOrgId();
        return paymentMilestoneRepository.findIncomingForOrg(orgId).stream()
                .map(this::toIncomingResponse)
                .toList();
    }

    private IncomingTransactionResponseDTO toIncomingResponse(PaymentMilestone milestone) {
        IncomingTransactionResponseDTO dto = new IncomingTransactionResponseDTO();
        dto.setMilestoneUid(milestone.getUid());
        dto.setDealUid(milestone.getDeal().getUid());
        dto.setEscapeUid(milestone.getDeal().getEscape().getUid());
        dto.setCustomerName(milestone.getDeal().getEscape().getLead().getName());
        dto.setCustomerEmail(milestone.getDeal().getEscape().getLead().getEmail());
        dto.setCustomerPhone(milestone.getDeal().getEscape().getLead().getPhone());
        dto.setLabel(milestone.getLabel());
        dto.setAmountInr(milestone.getAmountInr());
        dto.setAmountPaidInr(milestone.getAmountPaidInr());
        dto.setStatus(milestone.getStatus());
        dto.setPaymentMethod(milestone.getPaymentMethod());
        dto.setPaymentReference(milestone.getPaymentReference());
        dto.setMarkedPaidAt(milestone.getMarkedPaidAt());
        if (milestone.getMarkedPaidBy() != null) {
            userRepository.findById(milestone.getMarkedPaidBy()).map(User::getName).ifPresent(dto::setMarkedPaidByName);
        }
        return dto;
    }
}
