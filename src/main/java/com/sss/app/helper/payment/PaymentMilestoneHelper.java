package com.sss.app.helper.payment;

import com.sss.app.dto.payment.PaymentMilestoneCreateRequestDTO;
import com.sss.app.entity.deal.Deal;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.payment.PaymentMilestone;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
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
     * Records a (possibly partial) payment and recomputes both this
     * milestone's status and the trip's own lifecycle stage (Payment
     * Pending -> Partially Paid -> Fully Paid), forward-only — matches
     * Section 8's payment stages being driven by real milestone state
     * rather than a manual status field.
     */
    @Transactional
    public PaymentMilestone recordPayment(UUID uid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }
        PaymentMilestone milestone = getByUid(uid);

        BigDecimal newPaid = milestone.getAmountPaidUsd().add(amount);
        milestone.setAmountPaidUsd(newPaid);
        milestone.setStatus(newPaid.compareTo(milestone.getAmountUsd()) >= 0 ? "paid" : "partially_paid");
        milestone.setMarkedPaidBy(currentUser().getSeqp());
        milestone.setMarkedPaidAt(LocalDateTime.now());
        PaymentMilestone saved = paymentMilestoneRepository.save(milestone);

        auditLogService.record("Escape", milestone.getDeal().getEscape().getSeqp(), "PAYMENT_RECORDED",
                milestone.getLabel(), amount);

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
