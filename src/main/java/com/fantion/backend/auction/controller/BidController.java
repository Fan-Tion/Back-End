package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.BidDto;
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

    @GetMapping(value = "/{auctionId}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private SseEmitter subscribeBid(@PathVariable(name = "auctionId") Long auctionId){
        return bidService.subscribeBid(auctionId);
    }

    @PostMapping
    private BidDto.Response createBid(@RequestBody BidDto.Request request){
        log.info("[Controller] createBid");
        return  bidService.createBid(request);
    }

}
