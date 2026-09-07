package com.sss.app.service.billingtemplate;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.OrganizationsHelper;
import com.sss.app.service.BankAccountService;
import com.sss.app.service.deal.DealService;
import com.sss.app.service.quotationtemplate.QuotationDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Wraps QuotationDataService's data map with invoice-specific fields
 * (invoice number/date/status, itemized lines, amount paid/balance due)
 * instead of re-deriving organization/customer/trip/pricing/payment data
 * that map already assembles from real Escape/Quote/Deal data.
 */
@Service
@RequiredArgsConstructor
public class BillingDataService {

    private final QuotationDataService quotationDataService;
    private final DealService dealService;
    private final OrganizationsHelper organizationsHelper;
    private final BankAccountService bankAccountService;

    @SuppressWarnings("unchecked")
    public Map<String, Object> buildData(UUID escapeUid) {
        Map<String, Object> data = quotationDataService.buildData(escapeUid);

        try {
            dealService.getForEscape(escapeUid);
        } catch (NotFoundException e) {
            throw new BadRequestException("No deal found for this escape — cannot generate an invoice.");
        }

        Map<String, Object> pricing = (Map<String, Object>) data.get("pricing");
        // pricing is non-empty as soon as ANY quote exists on the itinerary,
        // even one never actually computed (total still null) — an invoice
        // needs a quote that's both accepted AND priced.
        if (pricing == null || pricing.isEmpty() || pricing.get("total") == null) {
            throw new BadRequestException("No priced, accepted quote found for this escape — cannot generate an invoice.");
        }

        BigDecimal subtotal = asDecimal(pricing.get("subtotal"));
        BigDecimal tax = asDecimal(pricing.get("tax"));
        BigDecimal tcs = asDecimal(pricing.get("tcs"));
        // GST and TCS are separate BigDecimal fields on Quote — combined
        // into one figure here so the invoice's single "Tax" column/row
        // (and its per-line allocation below) reflects the complete tax
        // charged, not just GST.
        BigDecimal taxTotal = tax.add(tcs);
        BigDecimal total = asDecimal(pricing.get("total"));
        // Backed out from the already-authoritative aggregate figures, so
        // it's correct whether the underlying discount was percent- or
        // flat-based — never re-derived from discountType/discountValue.
        BigDecimal discountAmount = subtotal.add(taxTotal).subtract(total).max(BigDecimal.ZERO);

        List<Map<String, Object>> milestones = (List<Map<String, Object>>) ((Map<String, Object>) data.get("payment")).get("milestones");
        NumberFormat inrFormat = inrWholeFormat();
        // QuotationDataService leaves milestone amounts as raw BigDecimal
        // (Mustache can't format numbers) — mutated in place here (this map
        // instance belongs only to this invoice render) so the Payment
        // Schedule section shows "83,200" instead of "83200.00".
        for (Map<String, Object> milestone : milestones) {
            milestone.put("amountFormatted", inrFormat.format(asDecimal(milestone.get("amount"))));
            milestone.put("amountPaidFormatted", inrFormat.format(asDecimal(milestone.get("amountPaid"))));
        }
        BigDecimal amountPaid = milestones.stream()
                .map(m -> asDecimal(m.get("amountPaid")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balanceDue = total.subtract(amountPaid).max(BigDecimal.ZERO);
        LocalDate dueDate = milestones.stream()
                .filter(m -> !"paid".equals(m.get("status")))
                .map(m -> (LocalDate) m.get("dueDate"))
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
        String invoiceStatus = balanceDue.compareTo(BigDecimal.ZERO) <= 0 ? "Paid"
                : amountPaid.compareTo(BigDecimal.ZERO) > 0 ? "Partially Paid" : "Unpaid";

        // pricing.subtotal/total only come pre-formatted for total/perPax in
        // QuotationDataService — added here too since the invoice summary
        // block shows every one of these lines.
        pricing.put("subtotalFormatted", inrFormat.format(subtotal));
        pricing.put("taxTotalFormatted", inrFormat.format(taxTotal));

        // One summary line per invoice (destination/qty/pricing), not a
        // day-by-day cost breakdown — the customer-facing invoice shouldn't
        // itemize every hotel/activity/transport booking the way the
        // itinerary/quotation views do.
        Map<String, Object> primaryEscapePoint = (Map<String, Object>) data.get("primaryEscapePoint");
        String destinationName = (String) primaryEscapePoint.get("name");
        String locationLabel = (String) primaryEscapePoint.get("locationLabel");
        String destination = locationLabel != null && !locationLabel.isBlank()
                ? destinationName + ", " + locationLabel
                : destinationName;

        int paxCount = ((Number) pricing.getOrDefault("paxCount", 0)).intValue();
        BigDecimal perPax = paxCount > 0 ? total.divide(BigDecimal.valueOf(paxCount), 2, RoundingMode.HALF_UP) : total;

        Map<String, Object> line = new LinkedHashMap<>();
        line.put("sNo", 1);
        line.put("description", destination);
        line.put("quantity", paxCount);
        line.put("unitPrice", perPax);
        line.put("unitPriceFormatted", inrFormat.format(perPax));
        line.put("tax", taxTotal);
        line.put("taxFormatted", inrFormat.format(taxTotal));
        line.put("discount", discountAmount);
        line.put("discountFormatted", inrFormat.format(discountAmount));
        line.put("amount", total);
        line.put("amountFormatted", inrFormat.format(total));
        List<Map<String, Object>> invoiceItems = List.of(line);

        String tripCode = (String) data.get("tripCode");
        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNumber", "INV-" + tripCode);
        invoice.put("invoiceDate", LocalDate.now());
        invoice.put("dueDate", dueDate);
        invoice.put("hasDueDate", dueDate != null);
        invoice.put("status", invoiceStatus);
        invoice.put("isPaid", "Paid".equals(invoiceStatus));
        invoice.put("isPartiallyPaid", "Partially Paid".equals(invoiceStatus));
        invoice.put("isUnpaid", "Unpaid".equals(invoiceStatus));
        invoice.put("discountAmount", discountAmount);
        invoice.put("discountAmountFormatted", inrFormat.format(discountAmount));
        invoice.put("hasDiscount", discountAmount.compareTo(BigDecimal.ZERO) > 0);
        invoice.put("amountPaid", amountPaid);
        invoice.put("amountPaidFormatted", inrFormat.format(amountPaid));
        invoice.put("balanceDue", balanceDue);
        invoice.put("balanceDueFormatted", inrFormat.format(balanceDue));
        invoice.put("items", invoiceItems);
        // Neither Lead nor Traveller stores a postal address today — an
        // honest placeholder for when that data exists, not a fabricated one.
        invoice.put("hasCustomerAddress", false);

        Organizations org = organizationsHelper.getMyOrganization();
        Integer paymentTermsDays = organizationsHelper.getSettings(org.getSeqp()).getDefaultPaymentTermsDays();
        invoice.put("paymentTermsDays", paymentTermsDays);
        invoice.put("hasPaymentTermsDays", paymentTermsDays != null);

        List<BankAccountDto> bankAccounts = bankAccountService.getAccountsForOrg(org.getUid());
        BankAccountDto bankAccount = bankAccounts.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .findFirst()
                .or(() -> bankAccounts.stream().findFirst())
                .orElse(null);
        if (bankAccount != null) {
            Map<String, Object> bank = new LinkedHashMap<>();
            bank.put("accountName", bankAccount.getAccountName());
            bank.put("accountNumber", bankAccount.getAccountNumber());
            bank.put("bankName", bankAccount.getBankName());
            bank.put("branchName", bankAccount.getBranchName());
            bank.put("ifsc", bankAccount.getIfsc());
            bank.put("swiftCode", bankAccount.getSwiftCode());
            invoice.put("bankDetails", bank);
        }
        invoice.put("hasBankDetails", bankAccount != null);

        data.put("invoice", invoice);
        return data;
    }

    private BigDecimal asDecimal(Object value) {
        return value instanceof BigDecimal ? (BigDecimal) value : BigDecimal.ZERO;
    }

    private NumberFormat inrWholeFormat() {
        NumberFormat format = NumberFormat.getInstance(new Locale("en", "IN"));
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format;
    }
}
