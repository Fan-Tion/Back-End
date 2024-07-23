package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.CategoryDto;
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
import org.springframework.data.domain.Page;
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

    Long result = auctionService.createAuction(request, auctionImage);
    ResultDTO<String> data = ResultDTO.of("경매 생성에 성공했습니다.", "auctionId: " + result);
    return ResponseEntity.ok(data);
  }

  @PutMapping("/{auctionId}")
  public ResponseEntity<?> updateAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage,
      @PathVariable("auctionId") Long auctionId) {
    AuctionDto.Response result = auctionService.updateAuction(request, auctionImage, auctionId);
    ResultDTO<AuctionDto.Response> data = ResultDTO.of("경매 업데이트 성공했습니다.", result);
    return ResponseEntity.ok(data);
  }

  @DeleteMapping("/{auctionId}")
  public ResponseEntity<?> deleteAuction(
      @PathVariable("auctionId") Long auctionId) {
    Boolean result = auctionService.deleteAuction(auctionId);
    ResultDTO<Boolean> data = ResultDTO.of("경매 삭제 성공했습니다.", result);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/favorite-category")
  public ResponseEntity<?> getFavoriteAuctionCategory() {
    List<CategoryDto> result = auctionService.getFavoriteAuctionCategory();
    ResultDTO<List<CategoryDto>> data = ResultDTO.of("인기 카테고리를 불러오는데 성공했습니다.", result);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/category")
  public ResponseEntity<?> getAllAuctionCategory() {
    List<CategoryDto> result = auctionService.getAllAuctionCategory();
    ResultDTO<List<CategoryDto>> data = ResultDTO.of("전체 카테고리를 불러오는데 성공했습니다.", result);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/list")
  public ResponseEntity<?> getAllAuctions(@Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    Page<AuctionDto.Response> result = auctionService.getList(page);
    ResultDTO<Page<AuctionDto.Response>> data = ResultDTO.of("경매 전체 페이지를 불러오는데 성공했습니다.", result);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/search")
  public ResponseEntity<?> searchAuctions(
      @RequestParam(name = "page", defaultValue = "0")int page,
      @RequestParam("searchOption") @NotNull SearchType searchOption,
      @RequestParam(name = "categoryOption", defaultValue = "ALL") CategoryType categoryOption,
      @RequestParam(name = "keyword", defaultValue = "") String keyword) {
    System.out.println(page + " " + searchOption + " " + categoryOption + " " + keyword);

    Page<AuctionDto.Response> result = auctionService.getSearchList(page, searchOption, categoryOption,
        keyword);
    ResultDTO<Page<AuctionDto.Response>> data = ResultDTO.of("경매 검색을 완료했습니다", result);
    return ResponseEntity.ok(data);
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
  public AuctionDto.Response findAuction(@PathVariable(name = "auctionId") Long auctionId) {
    return auctionService.findAuction(auctionId);
  }
}
