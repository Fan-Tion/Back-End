package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.entity.Auction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auction")
public class auctionController {

  @PostMapping
  public ResponseEntity<?> createAuction(
      @RequestBody AuctionDto.Request request) {
    return null;
  }

  @PutMapping("/{auctionId}")
  public ResponseEntity<?> updateAuction(
      @PathVariable("auctionId") Long auctionId,
      @RequestBody AuctionDto.Request request) {
    return null;
  }

  @DeleteMapping("/{auctionId}")
  public ResponseEntity<?> deleteAuction(
      @PathVariable("auctionId") Long auctionId) {
    return null;
  }

  @GetMapping("/list")
  public ResponseEntity<?> getAuctionList() {
    return null;
  }

  @GetMapping("/search")
  public ResponseEntity<?> searchAuction() {
    return null;
  }

  @GetMapping("/view/{auctionId}")
  public ResponseEntity<?> searchAuction(@PathVariable("auctionId") Long auctionId) {
    return null;
  }

  @PostMapping("/buy/{auctionId}")
  public ResponseEntity<?> buyAuction(@PathVariable("auctionId") Long auctionId) {
    return null;
  }

  @PutMapping("/bid/{auctionId}")
  public ResponseEntity<?> bidAuction(@PathVariable("auctionId") Long auctionId) {
    return null;
  }

  @PostMapping("/report/{auctionId}")
  public ResponseEntity<?> reportAuction(@PathVariable("auctionId") Long auctionId) {
    return null;
  }

  @PostMapping("/favorite/{auctionId}")
  public ResponseEntity<?> favoriteAuction(@PathVariable("auctionId") Long auctionId) {
    return null;
  }
}
