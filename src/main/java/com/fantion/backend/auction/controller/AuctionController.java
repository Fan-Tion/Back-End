package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.member.MemberRepository;
import com.fantion.backend.member.entity.Member;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auction")
public class AuctionController {

  private final AuctionService auctionService;
  private final MemberRepository memberRepository;

  @PostMapping("")
  public ResponseEntity<?> createAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage)
      throws IOException {
    memberRepository.save(new Member(1L, "email"));

    return ResponseEntity.ok(auctionService.createAuction(request, auctionImage));
  }

  @PutMapping("/{auctionId}")
  public ResponseEntity<?> updateAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage,
      @PathVariable("auctionId") Long auctionId) throws IOException {
    return ResponseEntity.ok(auctionService.updateAuction(request, auctionImage, auctionId));
  }

  @DeleteMapping("/{auctionId}")
  public ResponseEntity<?> deleteAuction(
      @PathVariable("auctionId") Long auctionId) throws IOException {
    return ResponseEntity.ok(auctionService.deleteAuction(auctionId));
  }
}
