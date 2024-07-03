package com.fantion.backend.payment.service.impl;

import com.fantion.backend.payment.domain.Account;
import com.fantion.backend.payment.domain.Transaction;
import com.fantion.backend.payment.dto.*;
import com.fantion.backend.payment.exception.AccountNotFoundException;
import com.fantion.backend.payment.repository.AccountRepository;
import com.fantion.backend.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    @CacheEvict(value = "account", key = "#depositReqDTO.accountId", condition = "#result != null")
    public TransactionDTO deposit(@NotNull DepositReqDTO depositReqDTO) {
        Account account = getAccount(depositReqDTO.getAccountId());
        Transaction transaction = transactionRepository.save(depositReqDTO.toTransactionDTO(account).toEntity(account));
        accountRepository.save(account.applyTransaction(transaction));

        return transaction.toDTO();
    }

    @Override
    @Transactional
    @CacheEvict(value = "account", key = "#withdrawalReqDTO.accountId", condition = "#result != null")
    public TransactionDTO withdraw(@NotNull WithdrawalReqDTO withdrawalReqDTO, @NotNull HoldingSummaryDTO holdingSummaryDTO) {
        Account account = getAccount(withdrawalReqDTO.getAccountId());
        Transaction transaction = transactionRepository.save(withdrawalReqDTO.toTransactionDTO(account, holdingSummaryDTO).toEntity(account));
        accountRepository.save(account.applyTransaction(transaction));

        return transaction.toDTO();
    }

    @Override
    public List<TransactionDTO> getTransactionList(long accountId) {
        return transactionRepository.findByAccountId(accountId).stream().map(Transaction::toDTO).collect(Collectors.toList());
    }

    private Account getAccount(long accountId) {
        return accountRepository.findByIdAndValidityIsTrue(accountId).orElseThrow(AccountNotFoundException::new);
    }
}