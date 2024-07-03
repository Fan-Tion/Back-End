package com.fantion.backend.payment.dto;

import com.fantion.backend.payment.domain.Account;
import com.fantion.backend.payment.domain.Transaction;
import com.fantion.backend.payment.exception.AccountIllegalStateException;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DepositRequestDTO {

    private Long accountId;
    private Long amount;
    private String label;

    public TransactionDTO toTransactionDTO(Account account) {
        if (account.getStatusType() == Account.StatusType.FREEZING) throw new AccountIllegalStateException();

        return TransactionDTO.builder().accountId(this.accountId)
                .type(Transaction.Type.DEPOSIT)
                .amount(this.amount)
                .balance(account.getBalance() + this.amount)
                .label(this.label)
                .recordedDate(new Date()).build();
    }
}