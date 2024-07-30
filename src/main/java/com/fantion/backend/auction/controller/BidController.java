package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.*;
import com.fantion.backend.auction.service.BidService;
import com.fantion.backend.common.dto.ResultDTO;
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

    @GetMapping(value = "/{auctionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private SseEmitter subscribeBid(@PathVariable("auctionId") Long auctionId){
        return bidService.subscribeBid(auctionId);
    }

    @PostMapping
    private ResultDTO<BidDto.Response> createBid(@RequestBody BidDto.Request request){
        log.info("[Controller] createBid");
        return bidService.createBid(request);
    }

    @PutMapping
    private ResultDTO<BuyNowDto.Response> buyNow(@RequestBody BuyNowDto.Request request){
        log.info("[Controller] buyNow");
        return bidService.buyNow(request);
    }

    @DeleteMapping
    private ResultDTO<BidCancelDto.Response> cancelBid(@RequestBody BidCancelDto.Request request){
        log.info("[Controller] cancelBid");
        return bidService.cancelBid(request);
    }

    @GetMapping("/auction")
    private ResultDTO<BidSuccessListDto.Response> successBidAuctionList(){
        return bidService.successBidAuctionList();
    }

    @PostMapping("/auction")
    private ResultDTO<HandOverDto.Response> receiveChk(@RequestBody HandOverDto.Request request){
        return bidService.receiveChk(request);
    }

    @PutMapping("/auction")
    private ResultDTO<HandOverDto.Response> sendChk(@RequestBody HandOverDto.Request request){
        return bidService.sendChk(request);
    }

    @DeleteMapping("/auction")
    private ResultDTO<BidAuctionCancelDto.Response> cancelBidAuction(@RequestBody BidAuctionCancelDto.Request request){
        return bidService.cancelBidAuction(request);
    }

    @GetMapping("/balance")
    private ResultDTO<BalanceCheckDto.Response> useBalanceCheck(){
        return bidService.useBalanceCheck();
    }

}
