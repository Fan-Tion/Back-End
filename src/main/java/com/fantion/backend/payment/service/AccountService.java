package com.fantion.backend.payment.service;

import com.fantion.backend.payment.dto.*;

import java.util.List;

public interface AccountService {

    AccountDTO createAccount(AccountDTO accountDTO);
    AccountDTO getAccountInfo(long accountId);
    List<AccountDTO> getAccountInfoList(long userId);
    AccountDTO updateAccountInfo(AccountDTO accountDTO);
}