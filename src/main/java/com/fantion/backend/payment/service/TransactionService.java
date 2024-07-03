package com.fantion.backend.payment.service;

import com.fantion.backend.payment.dto.*;

import java.util.List;

public interface TransactionService {

    TransactionDTO deposit(DepositReqDTO depositReqDTO);
    TransactionDTO withdraw(WithdrawalReqDTO withdrawalReqDTO, HoldingSummaryDTO holdingSummaryDTO);
    List<TransactionDTO> getTransactionList(long accountId);
}