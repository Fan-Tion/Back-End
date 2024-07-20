package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.auction.service.impl.ReturnServiceImpl;
import com.fantion.backend.type.CategoryType;
import com.fantion.backend.type.SearchType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final ReturnServiceImpl returnService;


  @PostMapping("")
  public ResponseEntity<?> createAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage) {
    return ResponseEntity.ok(
        returnService.createAuctionReturn(
            auctionService.createAuction(request, auctionImage)));
  }

  @PutMapping("/{auctionId}")
  public ResponseEntity<?> updateAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage,
      @PathVariable("auctionId") Long auctionId) {
    return ResponseEntity.ok(
        returnService.updateAuctionReturn(
            auctionService.updateAuction(request, auctionImage, auctionId)));
  }

  @DeleteMapping("/{auctionId}")
  public ResponseEntity<?> deleteAuction(
      @PathVariable("auctionId") Long auctionId) {
    return ResponseEntity.ok(
        returnService.deleteAuctionReturn(
            auctionService.deleteAuction(auctionId)));
  }

  /**
   * 경매 종류 후 거래량 save or update
   * */
//  @PostMapping("/end-auction")
//  public ResponseEntity<?> endAuctionSaveOrUpdate(@RequestParam("categoryName") String categoryName) {
//    auctionService.endAuctionSaveOrUpdate(categoryName);
//    return ResponseEntity.ok("save or update");
//  }

  @GetMapping("/favorite-category")
  public ResponseEntity<?> getFavoriteAuctionCategory() {
    Map<String, Integer> map
        = auctionService.getAuctionDateValue();

    if (map == null) {
      map = new HashMap<>();
    }

    return ResponseEntity.ok(returnService.categoryReturn(
        "FAVORITE", auctionService.getFavoriteAuctionCategory(map)));
  }

  @GetMapping("/category")
  public ResponseEntity<?> getAllAuctionCategory() {
    return ResponseEntity.ok(
        returnService.categoryReturn(
            "ALL", auctionService.getAllAuctionCategory()));
  }

  @GetMapping("/list")
  public ResponseEntity<?> getAllAuctions(
      @Valid @RequestParam(value = "page", defaultValue = "0") int page
  ) {

    return ResponseEntity.ok(
        returnService.getAuctionListReturn(
            "ALL", auctionService.getList(page)));
  }

  @GetMapping("/search")
  public ResponseEntity<?> searchAuctions(
      @RequestParam(name = "page", defaultValue = "0")int page,
      @RequestParam("searchOption") @NotNull SearchType searchOption,
      @RequestParam(name = "categoryOption", defaultValue = "ALL") CategoryType categoryOption,
      @RequestParam(name = "keyword", defaultValue = "") String keyword) {
    System.out.println(page + " " + searchOption + " " + categoryOption + " " + keyword);

    return ResponseEntity.ok(
        returnService.getAuctionListReturn(
            "SEARCH", auctionService.getSearchList(page, searchOption, categoryOption, keyword)));
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
  public AuctionDto.Response findAuction(@PathVariable(name = "auctionId") Long auctionId){
      return auctionService.findAuction(auctionId);
  }
}
