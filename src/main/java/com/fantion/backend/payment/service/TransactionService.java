package com.fantion.backend.payment.service;

import com.fantion.backend.payment.dto.*;

import java.util.List;

public interface TransactionService {

    TransactionDTO deposit(DepositRequestDTO depositReqDTO);
    TransactionDTO withdraw(WithdrawalRequestDTO withdrawalReqDTO, HoldingSummaryDTO holdingSummaryDTO);
    List<TransactionDTO> getTransactionList(long accountId);
}