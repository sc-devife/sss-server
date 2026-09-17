package com.sss.app.service.transaction;

import com.sss.app.dto.transaction.IncomingTransactionResponseDTO;
import com.sss.app.dto.transaction.OutgoingTransactionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    List<IncomingTransactionResponseDTO> getIncomingTransactions();

    List<OutgoingTransactionResponseDTO> getOutgoingTransactions();

    List<OutgoingTransactionResponseDTO> getOutgoingTransactionsForEscape(UUID escapeUid);
}
