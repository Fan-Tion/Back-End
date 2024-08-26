package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.*;
import com.fantion.backend.auction.service.BidService;
import com.fantion.backend.common.dto.ResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/bid")
@RequiredArgsConstructor
@Tag(name = "Bid", description = "Bid Service API")
public class BidController {

    private final BidService bidService;
    @Operation(summary = "입찰내역 구독", description = "입찰내역을 구독할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 입찰내역을 구독했습니다.")
    @GetMapping(value = "/{auctionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private SseEmitter subscribeBid(@PathVariable("auctionId") Long auctionId, HttpServletResponse httpServletResponse){
        return bidService.subscribeBid(auctionId,httpServletResponse);
    }

    @Operation(summary = "입찰", description = "공개 또는 비공개 입찰할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 입찰되었습니다.")
    @PostMapping
    private ResultDTO<BidDto.Response> createBid(@RequestBody BidDto.Request request){
        log.info("[Controller] createBid");
        return bidService.createBid(request);
    }

    @Operation(summary = "즉시구매", description = "즉시구매할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 즉시구 매되었습니다.")
    @PutMapping
    private ResultDTO<BuyNowDto.Response> buyNow(@RequestBody BuyNowDto.Request request){
        log.info("[Controller] buyNow");
        return bidService.buyNow(request);
    }

    @Operation(summary = "입찰 취소", description = "비공개 입찰 취소할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 입찰 취소 되었습니다.")
    @DeleteMapping
    private ResultDTO<BidCancelDto.Response> cancelBid(@RequestBody BidCancelDto.Request request){
        log.info("[Controller] cancelBid");
        return bidService.cancelBid(request);
    }

    @Operation(summary = "거래중인 경매물품 조회", description = "거래중인 경매물품 조회할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 거래중인 경매물품이 조회되었습니다.")
    @GetMapping("/auction")
    private ResultDTO<BidSuccessListDto.Response> successBidAuctionList(){
        return bidService.successBidAuctionList();
    }

    @Operation(summary = "인수 확인", description = "구매자가 인수 확인할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 인수 확인되었습니다.")
    @PostMapping("/auction")
    private ResultDTO<HandOverDto.Response> receiveChk(@RequestBody HandOverDto.Request request){
        return bidService.receiveChk(request);
    }

    @Operation(summary = "인계 확인", description = "판매자가 인계 확인할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 인계 확인되었습니다.")
    @PutMapping("/auction")
    private ResultDTO<HandOverDto.Response> sendChk(@RequestBody HandOverDto.Request request){
        return bidService.sendChk(request);
    }

    @Operation(summary = "구매 철회", description = "구매자가 구매 철회할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 입찰되었습니다.")
    @DeleteMapping("/auction")
    private ResultDTO<BidAuctionCancelDto.Response> cancelBidAuction(@RequestBody BidAuctionCancelDto.Request request){
        return bidService.cancelBidAuction(request);
    }

    @Operation(summary = "사용 가능한 예치금 조회", description = "사용 가능한 예치금 조회할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 사용 가능한 예치금이 조회되었습니다.")
    @GetMapping("/balance")
    private ResultDTO<BalanceCheckDto.Response> useBalanceCheck(){
        return bidService.useBalanceCheck();
    }

}
