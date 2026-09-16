package com.sss.app.controller.transaction;

import com.sss.app.dto.transaction.IncomingTransactionResponseDTO;
import com.sss.app.dto.transaction.OutgoingTransactionResponseDTO;
import com.sss.app.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Section: Accounting > Transactions — a combined incoming/outgoing ledger.
// Incoming is backed by PaymentMilestone (customer payments); Outgoing is
// backed by HotelPayment (vendor/hotel payouts).
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping("/incoming")
    public ResponseEntity<List<IncomingTransactionResponseDTO>> getIncomingTransactions() {
        return ResponseEntity.ok(transactionService.getIncomingTransactions());
    }

    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping("/outgoing")
    public ResponseEntity<List<OutgoingTransactionResponseDTO>> getOutgoingTransactions() {
        return ResponseEntity.ok(transactionService.getOutgoingTransactions());
    }
}
