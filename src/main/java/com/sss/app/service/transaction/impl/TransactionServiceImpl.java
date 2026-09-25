package com.sss.app.service.transaction.impl;

import com.sss.app.dto.transaction.IncomingTransactionResponseDTO;
import com.sss.app.dto.transaction.OutgoingTransactionResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.library.activity.ActivityPayment;
import com.sss.app.entity.library.hotel.HotelPayment;
import com.sss.app.entity.payment.PaymentMilestone;
import com.sss.app.entity.users.User;
import com.sss.app.helper.escape.EscapeHelper;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.library.activity.ActivityPaymentRepository;
import com.sss.app.repository.library.hotel.HotelPaymentRepository;
import com.sss.app.repository.payment.PaymentMilestoneRepository;
import com.sss.app.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final PaymentMilestoneRepository paymentMilestoneRepository;
    private final HotelPaymentRepository hotelPaymentRepository;
    private final ActivityPaymentRepository activityPaymentRepository;
    private final UserRepository userRepository;
    private final EscapeHelper escapeHelper;
    private final com.sss.app.service.payment.PaymentRecordAssembler paymentRecordAssembler;

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
        dto.setAmountBase(milestone.getAmountBase());
        dto.setAmountPaidBase(milestone.getAmountPaidBase());
        dto.setStatus(milestone.getStatus());
        dto.setPaymentMethod(milestone.getPaymentMethod());
        dto.setPaymentReference(milestone.getPaymentReference());
        dto.setMarkedPaidAt(milestone.getMarkedPaidAt());
        java.util.List<com.sss.app.dto.payment.PaymentRecordResponseDTO> payments = paymentRecordAssembler.forMilestone(milestone.getSeqp());
        dto.setPayments(payments);
        dto.setFxDifferenceBase(payments.stream().map(com.sss.app.dto.payment.PaymentRecordResponseDTO::getFxDifferenceBase)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        if (milestone.getMarkedPaidBy() != null) {
            userRepository.findById(milestone.getMarkedPaidBy()).map(User::getName).ifPresent(dto::setMarkedPaidByName);
        }
        return dto;
    }

    @Override
    public List<OutgoingTransactionResponseDTO> getOutgoingTransactions() {
        Long orgId = currentUser().getOrgId();
        List<OutgoingTransactionResponseDTO> combined = new ArrayList<>();
        hotelPaymentRepository.findAllByOrgId(orgId).stream().map(this::toOutgoingResponse).forEach(combined::add);
        activityPaymentRepository.findAllByOrgId(orgId).stream().map(this::toOutgoingResponse).forEach(combined::add);
        combined.sort(Comparator.comparing(OutgoingTransactionResponseDTO::getPaymentDate).reversed());
        return combined;
    }

    // Backs the Escape Payments workspace's supplier-payments section — the
    // exact same rows getOutgoingTransactions() would show for this escape,
    // just pre-filtered server-side instead of scanning the whole org's
    // outgoing ledger client-side.
    @Override
    public List<OutgoingTransactionResponseDTO> getOutgoingTransactionsForEscape(UUID escapeUid) {
        Escape escape = escapeHelper.getEscapeById(escapeUid);
        List<OutgoingTransactionResponseDTO> combined = new ArrayList<>();
        hotelPaymentRepository.findAllByEscapeSeqp(escape.getSeqp()).stream().map(this::toOutgoingResponse).forEach(combined::add);
        activityPaymentRepository.findAllByEscapeSeqp(escape.getSeqp()).stream().map(this::toOutgoingResponse).forEach(combined::add);
        combined.sort(Comparator.comparing(OutgoingTransactionResponseDTO::getPaymentDate).reversed());
        return combined;
    }

    private OutgoingTransactionResponseDTO toOutgoingResponse(HotelPayment payment) {
        OutgoingTransactionResponseDTO dto = new OutgoingTransactionResponseDTO();
        dto.setPaymentUid(payment.getUid());
        dto.setVendorType("Hotel");
        dto.setVendorUid(payment.getHotel().getUid());
        dto.setVendorName(payment.getHotel().getName());
        dto.setEscapeUid(payment.getEscape().getUid());
        dto.setTripCode(payment.getEscape().getTripCode());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setAmount(payment.getAmount());
        dto.setPaidAmount(payment.getPaidAmount());
        dto.setPaidCurrency(payment.getPaidCurrency());
        dto.setFxRate(payment.getFxRate());
        dto.setPaidBy(payment.getPaidBy());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setNotes(payment.getNotes());
        dto.setStatus(payment.getStatus());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }

    private OutgoingTransactionResponseDTO toOutgoingResponse(ActivityPayment payment) {
        OutgoingTransactionResponseDTO dto = new OutgoingTransactionResponseDTO();
        dto.setPaymentUid(payment.getUid());
        dto.setVendorType("Activity");
        dto.setVendorUid(payment.getActivity().getUid());
        dto.setVendorName(payment.getActivity().getName());
        dto.setEscapeUid(payment.getEscape().getUid());
        dto.setTripCode(payment.getEscape().getTripCode());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setAmount(payment.getAmount());
        dto.setPaidAmount(payment.getPaidAmount());
        dto.setPaidCurrency(payment.getPaidCurrency());
        dto.setFxRate(payment.getFxRate());
        dto.setPaidBy(payment.getPaidBy());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setNotes(payment.getNotes());
        dto.setStatus(payment.getStatus());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}
