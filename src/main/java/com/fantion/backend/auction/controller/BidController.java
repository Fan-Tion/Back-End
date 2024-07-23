package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.*;
import com.fantion.backend.auction.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/bid")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private SseEmitter subscribeBid(@RequestBody BidSubscribeDto.Request request){
        return bidService.subscribeBid(request);
    }

    @PostMapping
    private BidDto.Response createBid(@RequestBody BidDto.Request request){
        log.info("[Controller] createBid");
        return bidService.createBid(request);
    }

    @PutMapping
    private BuyNowDto.Response buyNow(@RequestBody BuyNowDto.Request request){
        log.info("[Controller] buyNow");
        return bidService.buyNow(request);
    }

    @DeleteMapping
    private BidCancelDto.Response cancelBid(@RequestBody BidCancelDto.Request request){
        log.info("[Controller] cancelBid");
        return bidService.cancelBid(request);
    }

    @GetMapping("/auction")
    private SuccessBidListDto.Response successBidAuctionList(){
        return bidService.successBidAuctionList();
    }

    @PostMapping("/auction")
    private HandOverDto.Response receiveChk(@RequestBody HandOverDto.Request request){
        return bidService.receiveChk(request);
    }

    @PutMapping("/auction")
    private HandOverDto.Response sendChk(@RequestBody HandOverDto.Request request){
        return bidService.sendChk(request);
    }

    @GetMapping("/balance")
    private BalanceCheckDto.Response useBalanceCheck(){
        return bidService.useBalanceCheck();
    }

}
