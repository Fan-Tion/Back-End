package com.fantion.backend.payment.service;

import com.fantion.backend.payment.dto.HoldingDTO;
import com.fantion.backend.payment.dto.HoldingSummaryDTO;

import java.util.Date;
import java.util.List;

public interface HoldingService {

    HoldingSummaryDTO getHoldingSummaryInfo(long accountId);
    HoldingSummaryDTO getHoldingSummaryInfo(long accountId, Date date);
    HoldingDTO getHoldingInfo(long holdingId);
    List<HoldingDTO> getHoldingList(long accountId);
    List<HoldingDTO> getHoldingList(long accountId, Date date);
    HoldingDTO createHolding(HoldingDTO holdingDTO);
    HoldingDTO changeHoldingStatusClosed(long holdingId);
    HoldingDTO extendHolding(long holdingId, Date newExpiredDate);
}