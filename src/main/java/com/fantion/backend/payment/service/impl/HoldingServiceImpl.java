package com.fantion.backend.payment.service.impl;

import com.fantion.backend.payment.domain.Account;
import com.fantion.backend.payment.domain.Holding;
import com.fantion.backend.payment.dto.HoldingDTO;
import com.fantion.backend.payment.dto.HoldingSummaryDTO;
import com.fantion.backend.payment.exception.AccountNotFoundException;
import com.fantion.backend.payment.exception.HoldingNotFoundException;
import com.fantion.backend.payment.repository.AccountRepository;
import com.fantion.backend.payment.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;

    @Override
    public HoldingSummaryDTO getHoldingSummaryInfo(long accountId) {
        return getHoldingSummaryInfo(accountId, new Date());
    }

    @Override
    public HoldingSummaryDTO getHoldingSummaryInfo(long accountId, @NotNull Date date) {
        return new HoldingSummaryDTO(getHoldingList(accountId, date));
    }

    @Override
    public HoldingDTO getHoldingInfo(long holdingId) {
        return getHolding(holdingId).toDTO();
    }

    @Override
    public List<HoldingDTO> getHoldingList(long accountId) {
        return getHoldingList(accountId, new Date());
    }

    @Override
    public List<HoldingDTO> getHoldingList(long accountId, @NotNull Date date) {
        return holdingRepository.findByAccountIdAndExpiredDateAfter(accountId, date).stream().map(Holding::toDTO).collect(Collectors.toList());
    }

    @Override
    public HoldingDTO createHolding(@NotNull HoldingDTO holdingDTO) {
        return holdingRepository.save(holdingDTO.toEntity(getAccount(holdingDTO.getAccountId()),
                getHoldingSummaryInfo(holdingDTO.getAccountId()))).toDTO();
    }

    @Override
    public HoldingDTO changeHoldingStatusClosed(long holdingId) {
        return holdingRepository.save(getHolding(holdingId).updateStatusClosed()).toDTO();
    }

    @Override
    public HoldingDTO extendHolding(long holdingId, @NotNull Date newExpiredDate) {
        return holdingRepository.save(getHolding(holdingId).updateExpiredDate(newExpiredDate)).toDTO();
    }

    private Account getAccount(long accountId) {
        return accountRepository.findByIdAndValidityIsTrue(accountId).orElseThrow(AccountNotFoundException::new);
    }

    private Holding getHolding(long holdingId) {
        return holdingRepository.findById(holdingId).orElseThrow(HoldingNotFoundException::new);
    }
}