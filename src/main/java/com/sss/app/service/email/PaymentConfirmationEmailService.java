package com.sss.app.service.email;

import com.sss.app.entity.deal.Deal;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.payment.PaymentMilestone;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.payment.PaymentMilestoneRepository;
import com.sss.app.service.quotationtemplate.QuotationRenderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Sends the "payment confirmed" notification after PaymentMilestoneHelper.
 * verifyPayment() succeeds — never on record-payment (still "unverified"),
 * never if verification itself throws. Deliberately @Async: the caller
 * (PaymentMilestoneServiceImpl.verifyPayment) already returned its HTTP
 * response by the time this runs, so a slow SMTP round trip (observed up to
 * ~25s against this org's mail server) never blocks the "Verify Payment"
 * click. Takes the milestone's UUID rather than the entity the caller already
 * has — the caller's transaction (and Hibernate session) closes before this
 * async method starts, so lazy relations (deal.escape.lead/travellers) must
 * be loaded fresh in this method's own transaction, not read off a detached
 * entity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmationEmailService {

    private static final String EMAIL_BODY_TEMPLATE = "email-templates/payment-confirmation-email.mustache";
    private static final String EMAIL_SUBJECT_TEMPLATE = "Payment Confirmation — {{tripCode}}";

    private final PaymentMilestoneRepository paymentMilestoneRepository;
    private final OrganizationRepository organizationRepository;
    private final QuotationRenderingService quotationRenderingService;
    private final EmailService emailService;
    private final com.sss.app.service.exchangerate.CurrencyDisplayService currencyDisplayService;
    private final com.sss.app.service.exchangerate.MoneyFormatter moneyFormatter;

    @Async
    @Transactional(readOnly = true)
    public void sendPaymentConfirmationEmail(UUID milestoneUid) {
        try {
            PaymentMilestone milestone = paymentMilestoneRepository.findByUid(milestoneUid).orElse(null);
            if (milestone == null) {
                return; // deleted between verify and this async run — nothing to notify about
            }

            Deal deal = milestone.getDeal();
            Escape escape = deal.getEscape();

            Recipient recipient = resolveRecipient(escape);
            if (recipient == null) {
                log.info("Skipping payment confirmation email for milestone {} — no email on the primary traveller or lead", milestoneUid);
                return;
            }

            BigDecimal totalBase = deal.getAcceptedQuote() != null && deal.getAcceptedQuote().getTotalBase() != null
                    ? deal.getAcceptedQuote().getTotalBase() : BigDecimal.ZERO;
            List<PaymentMilestone> allMilestones = paymentMilestoneRepository.findAllByDeal_Seqp(deal.getSeqp());
            BigDecimal totalPaid = allMilestones.stream()
                    .map(PaymentMilestone::getAmountPaidBase)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal pendingAmount = totalBase.subtract(totalPaid).max(BigDecimal.ZERO);

            Organizations org = organizationRepository.findById(milestone.getOrgId()).orElse(null);
            String orgName = org != null
                    ? (org.getDisplayName() != null && !org.getDisplayName().isBlank() ? org.getDisplayName() : org.getRegisteredName())
                    : "";

            EscapePoint destination = escape.getEscapePoints() == null ? null
                    : escape.getEscapePoints().stream().findFirst().orElse(null);

            NumberFormat moneyFormat = moneyFormat(milestone.getOrgId());
            Map<String, Object> data = new HashMap<>();
            data.put("recipientName", recipient.name());
            data.put("organizationName", orgName);
            data.put("tripCode", escape.getTripCode());
            data.put("destinationName", destination != null ? destination.getName() : null);
            data.put("milestoneLabel", milestone.getLabel());
            data.put("paidAmountFormatted", moneyFormat.format(milestone.getAmountPaidBase()));
            data.put("pendingAmountFormatted", moneyFormat.format(pendingAmount));
            data.put("isFullyPaid", pendingAmount.compareTo(BigDecimal.ZERO) <= 0);
            String baseCode = currencyDisplayService.baseCurrencyCode(milestone.getOrgId());
            data.put("currencyCode", baseCode);
            data.put("currencySymbol", currencyDisplayService.symbol(baseCode));

            String subject = quotationRenderingService.renderInline(EMAIL_SUBJECT_TEMPLATE, data);
            String body = quotationRenderingService.renderClasspathTemplate(EMAIL_BODY_TEMPLATE, data);
            emailService.sendHtmlEmail(List.of(recipient.email()), subject, body);
        } catch (Exception e) {
            // Best-effort notification — must never surface as a failure of
            // the payment verification itself, which has already succeeded
            // and committed by the time this runs.
            log.error("Failed to send payment confirmation email for milestone {}", milestoneUid, e);
        }
    }

    private record Recipient(String name, String email) {}

    // Primary traveller's email if set, else the lead's — never both, and
    // never any other traveller (unlike the Quotation/Invoice "send to
    // everyone" email, this notification goes to exactly one person).
    private Recipient resolveRecipient(Escape escape) {
        UUID primaryTravellerUid = escape.getPrimaryTravellerUid();
        if (primaryTravellerUid != null && escape.getTravellers() != null) {
            for (Traveller traveller : escape.getTravellers()) {
                if (primaryTravellerUid.equals(traveller.getUid())
                        && traveller.getEmail() != null && !traveller.getEmail().isBlank()) {
                    String name = travellerDisplayName(traveller);
                    return new Recipient(name, traveller.getEmail().trim());
                }
            }
        }
        Lead lead = escape.getLead();
        if (lead != null && lead.getEmail() != null && !lead.getEmail().isBlank()) {
            return new Recipient(lead.getName() != null ? lead.getName() : "Guest", lead.getEmail().trim());
        }
        return null;
    }

    private String travellerDisplayName(Traveller traveller) {
        String first = traveller.getFirstName() != null ? traveller.getFirstName().trim() : "";
        String last = traveller.getLastName() != null ? traveller.getLastName().trim() : "";
        String name = (first + " " + last).trim();
        return name.isBlank() ? "Guest" : name;
    }

    private java.text.NumberFormat moneyFormat(Long orgId) {
        return moneyFormatter.forOrg(orgId, null);
    }
}
