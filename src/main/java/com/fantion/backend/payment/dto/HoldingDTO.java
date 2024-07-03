package com.fantion.backend.payment.dto;

import com.fantion.backend.payment.domain.Account;
import com.fantion.backend.payment.domain.Holding;
import com.fantion.backend.payment.exception.AccountBalanceShortageException;
import com.fantion.backend.payment.exception.AccountIllegalStateException;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class HoldingDTO {

    private Long id;
    private Long accountId;
    private Long amount;
    private Long balance;
    private Date expiredDate;
    private Date recordedDate;
    private Date lastModifiedDate;
    private Holding.StatusType statusType;

    public Holding toEntity(Account account, HoldingSummaryDTO holdingSummaryDTO) {
        if (account.getStatusType() != Account.StatusType.NORMAL) throw new AccountIllegalStateException();
        if (account.getBalance() - holdingSummaryDTO.getAmount() < this.amount) throw new AccountBalanceShortageException();

        return Holding.builder().account(account)
                .amount(this.amount)
                .balance(account.getBalance())
                .expiredDate(this.expiredDate)
                .recordedDate(new Date())
                .lastModifiedDate(new Date())
                .statusType(Holding.StatusType.HOLDED).build();
    }
}