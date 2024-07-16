package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.BalanceCheckDto;
import com.fantion.backend.auction.dto.BidDto;
import com.fantion.backend.auction.dto.BidSubscribeDto;
import com.fantion.backend.auction.dto.BuyNowDto;
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
        return  bidService.createBid(request);
    }

    @PutMapping
    private BuyNowDto.Response buyNow(@RequestBody BuyNowDto.Request request){
        log.info("[Controller] buyNow");
        return  bidService.buyNow(request);
    }

    @GetMapping("/balance")
    private BalanceCheckDto.Response useBalanceCheck(){
        return bidService.useBalanceCheck();
    }

}
