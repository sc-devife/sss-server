package com.sss.app.helper.payment;

import com.sss.app.dto.payment.PaymentMilestoneCreateRequestDTO;
import com.sss.app.entity.deal.Deal;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.payment.PaymentMilestone;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.deal.DealHelper;
import com.sss.app.repository.payment.PaymentMilestoneRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.escape.EscapeLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentMilestoneHelper {

    private final PaymentMilestoneRepository paymentMilestoneRepository;
    private final DealHelper dealHelper;
    private final OrgAccessGuard orgAccessGuard;
    private final AuditLogService auditLogService;
    private final EscapeLifecycleService escapeLifecycleService;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public PaymentMilestone create(PaymentMilestoneCreateRequestDTO request) {
        Deal deal = dealHelper.getByUid(request.getDealUid());
        if ("cancelled".equals(deal.getStatus())) {
            throw new ConflictException("This deal is cancelled; payment milestones can no longer be added");
        }

        PaymentMilestone milestone = PaymentMilestone.builder()
                .orgId(deal.getOrgId())
                .deal(deal)
                .label(request.getLabel())
                .dueDate(request.getDueDate())
                .amountUsd(request.getAmountUsd())
                .amountPaidUsd(BigDecimal.ZERO)
                .status("pending")
                .build();

        return paymentMilestoneRepository.save(milestone);
    }

    public PaymentMilestone getByUid(UUID uid) {
        PaymentMilestone milestone = paymentMilestoneRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Payment milestone not found"));
        orgAccessGuard.requireAccessToOrg(milestone.getOrgId());
        return milestone;
    }

    public List<PaymentMilestone> getAllForDeal(UUID dealUid) {
        Deal deal = dealHelper.getByUid(dealUid);
        return paymentMilestoneRepository.findAllByOrgIdAndDeal_SeqpOrderByDueDateAsc(deal.getOrgId(), deal.getSeqp());
    }

    public void delete(UUID uid) {
        PaymentMilestone milestone = getByUid(uid);
        paymentMilestoneRepository.delete(milestone);
    }

    /**
     * Records a (possibly partial) payment. Lands in "unverified" rather
     * than "paid"/"partially_paid" directly — an agent recording a payment
     * isn't the same as finance confirming it landed; verifyPayment() below
     * is the second step that actually advances the trip's payment stage.
     */
    @Transactional
    public PaymentMilestone recordPayment(UUID uid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }
        PaymentMilestone milestone = getByUid(uid);
        if ("cancelled".equals(milestone.getDeal().getStatus())) {
            throw new ConflictException("This deal is cancelled; payments can no longer be recorded");
        }

        BigDecimal newPaid = milestone.getAmountPaidUsd().add(amount);
        milestone.setAmountPaidUsd(newPaid);
        milestone.setStatus("unverified");
        milestone.setMarkedPaidBy(currentUser().getSeqp());
        milestone.setMarkedPaidAt(LocalDateTime.now());
        PaymentMilestone saved = paymentMilestoneRepository.save(milestone);

        auditLogService.record("Escape", milestone.getDeal().getEscape().getSeqp(), "PAYMENT_RECORDED",
                milestone.getLabel(), amount);

        return saved;
    }

    /**
     * Finance confirms a recorded payment actually landed — only now does
     * the milestone move to its real paid/partially_paid status and the
     * trip's own payment lifecycle (Payment Pending -> Partially Paid ->
     * Fully Paid) get a chance to advance.
     */
    @Transactional
    public PaymentMilestone verifyPayment(UUID uid) {
        PaymentMilestone milestone = getByUid(uid);
        if (!"unverified".equals(milestone.getStatus())) {
            throw new BadRequestException("Only an unverified payment can be verified");
        }

        milestone.setStatus(milestone.getAmountPaidUsd().compareTo(milestone.getAmountUsd()) >= 0 ? "paid" : "partially_paid");
        PaymentMilestone saved = paymentMilestoneRepository.save(milestone);

        auditLogService.record("Escape", milestone.getDeal().getEscape().getSeqp(), "PAYMENT_VERIFIED",
                "unverified", saved.getStatus());

        advanceEscapePaymentStatus(milestone.getDeal());

        return saved;
    }

    private void advanceEscapePaymentStatus(Deal deal) {
        Escape trip = deal.getEscape();
        List<PaymentMilestone> allMilestones = paymentMilestoneRepository.findAllByDeal_Seqp(deal.getSeqp());
        if (allMilestones.isEmpty()) return;

        boolean allPaid = allMilestones.stream().allMatch(m -> "paid".equals(m.getStatus()));
        boolean anyPayment = allMilestones.stream().anyMatch(m -> m.getAmountPaidUsd().compareTo(BigDecimal.ZERO) > 0);

        String target = allPaid ? EscapeStatus.FULLY_PAID : anyPayment ? EscapeStatus.PARTIALLY_PAID : null;
        if (target == null) return;

        int currentIndex = EscapeStatus.indexOf(trip.getStatus());
        int targetIndex = EscapeStatus.indexOf(target);
        if (currentIndex >= 0 && targetIndex > currentIndex) {
            escapeLifecycleService.advance(trip.getUid(), target);
        }
    }
}
