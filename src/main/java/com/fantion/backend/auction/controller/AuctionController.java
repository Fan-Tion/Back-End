package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionFavoriteDto;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.type.CategoryType;
import com.fantion.backend.type.SearchType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@Validated
@RequestMapping("/auction")
@RequiredArgsConstructor
public class AuctionController {

  private final Path basePath = Paths.get("images/auction");

  private final AuctionService auctionService;


  @PostMapping("")
  public ResponseEntity<?> createAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage) {
    return ResponseEntity.ok(auctionService.createAuction(request, auctionImage));
  }

  @PutMapping("/{auctionId}")
  public ResponseEntity<?> updateAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage,
      @PathVariable("auctionId") Long auctionId) {
    return ResponseEntity.ok(auctionService.updateAuction(request, auctionImage, auctionId));
  }

  @DeleteMapping("/{auctionId}")
  public ResponseEntity<?> deleteAuction(
      @PathVariable("auctionId") Long auctionId) {
    return ResponseEntity.ok(auctionService.deleteAuction(auctionId));
  }

  @GetMapping("/favorite-category")
  public ResponseEntity<?> getFavoriteAuctionCategory() {
    return ResponseEntity.ok(auctionService.getFavoriteAuctionCategory());
  }

  @GetMapping("/category")
  public ResponseEntity<?> getAllAuctionCategory() {
    return ResponseEntity.ok(auctionService.getAllAuctionCategory());
  }

  @GetMapping("/list")
  public ResponseEntity<?> getAllAuctions(
      @Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return ResponseEntity.ok(auctionService.getList(page));
  }

  @GetMapping("/search")
  public ResponseEntity<?> searchAuctions(
      @RequestParam(name = "page", defaultValue = "0")int page,
      @RequestParam("searchOption") @NotNull SearchType searchOption,
      @RequestParam(name = "categoryOption", defaultValue = "ALL") CategoryType categoryOption,
      @RequestParam(name = "keyword", defaultValue = "") String keyword) {
    return ResponseEntity.ok(auctionService.getSearchList(page, searchOption, categoryOption, keyword));
  }

  /**
   * 이미지 가져오기
   */
  @GetMapping("/images/auction/{userId}/{filename:.+}")
  public ResponseEntity<Resource> getImage(
      @PathVariable("userId") String userId,
      @PathVariable("filename") String filename) {
    Path imagePath = basePath.resolve(userId).resolve(filename);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg");

    return ResponseEntity.ok()
        .headers(headers)
        .body(auctionService.getImage(imagePath, headers));
  }

  @GetMapping("/view/{auctionId}")
  public ResultDTO<AuctionDto.Response> findAuction(@PathVariable(name = "auctionId") Long auctionId) {
    return auctionService.findAuction(auctionId);
  }
  @GetMapping("/favorite/{auctionId}")
  public ResultDTO<AuctionFavoriteDto.Response> favoriteChk(@PathVariable(name = "auctionId") Long auctionId) {
    return auctionService.favoriteChk(auctionId);
  }
  @PostMapping("/favorite/{auctionId}")
  public ResultDTO<AuctionFavoriteDto.Response> favoriteAuction(@PathVariable(name = "auctionId") Long auctionId) {
    return auctionService.favoriteAuction(auctionId);
  }

  @GetMapping("/sell-auction-list")
  public ResultDTO<Page<AuctionDto.Response>> getSellAuctionList(@Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return auctionService.getSellAuctionList(page);
  }

  @GetMapping("/buy-auction-list")
  public ResultDTO<Page<AuctionDto.Response>> getBuyAuctionList(@Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return auctionService.getBuyAuctionList(page);
  }

  @GetMapping("/join-auction-list")
  public ResultDTO<Page<AuctionDto.Response>> getJoinAuctionList(@Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return auctionService.getJoinAuctionList(page);
  }

  @GetMapping("/favorite-auction-list")
  public ResultDTO<Page<AuctionDto.Response>> getFavoriteAuctionList(@Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return auctionService.getFavoriteAuctionList(page);
  }



}
