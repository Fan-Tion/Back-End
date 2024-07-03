package com.fantion.backend.payment.dto;

import com.fantion.backend.payment.domain.Account;
import com.fantion.backend.payment.domain.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TransactionDTO {

    private Long id;
    private Long accountId;
    private Transaction.Type type;
    private Long amount;
    private Long balance;
    private String label;
    private Date recordedDate;

    public Transaction toEntity(Account account) {
        return Transaction.builder().account(account)
                .amount(this.amount)
                .balance(this.balance)
                .type(this.type)
                .label(this.label)
                .recordedDate(new Date()).build();
    }
}