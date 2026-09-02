package com.sss.app.controller.transaction;

import com.sss.app.dto.transaction.IncomingTransactionResponseDTO;
import com.sss.app.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Section: Accounting > Transactions — a combined incoming/outgoing ledger.
// Incoming is backed by PaymentMilestone (customer payments already have
// their own full lifecycle there); Outgoing (vendor/supplier payments) has
// no backing data model yet, so only /incoming exists so far.
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
}
