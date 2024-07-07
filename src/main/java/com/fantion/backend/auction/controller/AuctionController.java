package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.BidDto;
import com.fantion.backend.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/auction")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PutMapping("/bid")
    private BidDto.Response createBid(@RequestBody BidDto.Request request){
        log.info("[Controller] createBid");
        return  auctionService.createBid(request);
    }

    @GetMapping("/view/{auctionId}")
    private AuctionDto.Response findAuction(@PathVariable(name = "auctionId") Long auctionId){
        return auctionService.findAuction(auctionId);
    }

}
