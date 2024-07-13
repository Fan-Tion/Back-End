package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.type.CategoryType;
import com.fantion.backend.type.MemberStatus;
import com.fantion.backend.type.SearchType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
  private final MemberRepository memberRepository;

  /**
   * 경매 생성
   */
  @PostMapping("")
  public ResponseEntity<?> createAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage) {
    memberRepository.save(
        new Member(1L, "email",
            "password", "nickname",
            true, true, true,
            "address", 0, 0, 0,
            MemberStatus.ACTIVE, null, LocalDateTime.now()));

    return ResponseEntity.ok(auctionService.createAuction(request, auctionImage));
  }

  /**
   * 경매 수정
   */
  @PutMapping("/{auctionId}")
  public ResponseEntity<?> updateAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage,
      @PathVariable("auctionId") Long auctionId) {
    return ResponseEntity.ok(auctionService.updateAuction(request, auctionImage, auctionId));
  }

  /**
   * 경매 삭제
   */
  @DeleteMapping("/{auctionId}")
  public ResponseEntity<?> deleteAuction(
      @PathVariable("auctionId") Long auctionId) {
    return ResponseEntity.ok(auctionService.deleteAuction(auctionId));
  }

  /**
   * 경매 종류 후 거래량 save or update
   * */
  @PostMapping("/end-auction")
  public ResponseEntity<?> endAuctionSaveOrUpdate(@RequestParam("value") String value) {
    auctionService.endAuctionSaveOrUpdate(value);
    return ResponseEntity.ok("save or update");
  }

  /**
   * 거래량 가져오기
   * */
  @GetMapping("/get")
  public ResponseEntity<?> getAuctionDateValue() {
    Map<String, Integer> map
        = auctionService.getAuctionDateValue();

    if (map == null) {
      map = new HashMap<>();
    }

    List<String> auctionBuyCountList = auctionService.mapToListLimitFive(map);

    return ResponseEntity.ok(auctionBuyCountList);
  }

  /**
   * 경매 리스트
   * */
  @GetMapping("/list")
  public ResponseEntity<?> getAllAuctions(
      @Valid @RequestParam(value = "page", defaultValue = "0") int page
  ) {
    return ResponseEntity.ok(auctionService.getList(page));
  }

  /**
   * 경매 검색
   * */
  @GetMapping("/search")
  public ResponseEntity<?> searchAuctions(
      @RequestParam("page") @Min(0) int page,
      @RequestParam("searchType") @NotNull SearchType searchOption,
      @RequestParam("categoryType") @NotNull CategoryType categoryOption,
      @RequestParam("keyword") String keyword) {
    return ResponseEntity.ok(auctionService
        .getSearchList(page, searchOption, categoryOption, keyword));
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
  private AuctionDto.Response findAuction(@PathVariable(name = "auctionId") Long auctionId){
      return auctionService.findAuction(auctionId);
  }
}
