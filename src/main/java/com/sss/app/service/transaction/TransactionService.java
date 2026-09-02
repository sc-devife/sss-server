package com.sss.app.service.transaction;

import com.sss.app.dto.transaction.IncomingTransactionResponseDTO;

import java.util.List;

public interface TransactionService {
    List<IncomingTransactionResponseDTO> getIncomingTransactions();
}
