package com.fantion.backend.auction.controller;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionFavoriteDto;
import com.fantion.backend.auction.dto.AuctionReportDto;
import com.fantion.backend.auction.dto.AuctionReportDto.AuctionReportResponse;
import com.fantion.backend.auction.dto.CategoryDto;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.exception.ErrorResponse;
import com.fantion.backend.type.CategoryType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@Tag(name = "Auction", description = "Auction Service API")
public class AuctionController {

  private final Path basePath = Paths.get("images/auction");
  private final AuctionService auctionService;


  @Operation(summary = "경매 생성", description = "경매 생성 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "경매 생성에 성공했습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.<br>존재하지 않는 경매입니다",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ResultDTO<Map<String, Long>>> createAuction(
      @Valid @RequestPart("request") AuctionDto.AuctionRequest request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage) {
    return ResponseEntity.ok(auctionService.createAuction(request, auctionImage));
  }

  @Operation(summary = "경매 수정", description = "경매 수정 할 때 사용하는 API")
  @ApiResponse(responseCode = "200", description = "성공적으로 경매를 변경했습니다.")
  @PutMapping(value = "/{auctionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ResultDTO<AuctionDto.AuctionResponse>> updateAuction(
      @Valid @RequestPart("request") AuctionDto.AuctionRequest request,
      @RequestPart Map<String, MultipartFile> auctionImage,
      @PathVariable("auctionId") Long auctionId) {
    return ResponseEntity.ok(auctionService.updateAuction(request, auctionImage, auctionId));
  }

  @Operation(summary = "경매 삭제", description = "경매 삭제 할 때 사용하는 API.")
  @ApiResponse(responseCode = "200", description = "성공적으로 경매를 변경했습니다.")
  @DeleteMapping("/{auctionId}")
  public ResponseEntity<ResultDTO<Boolean>> deleteAuction(
      @PathVariable("auctionId") Long auctionId) {
    return ResponseEntity.ok(auctionService.deleteAuction(auctionId));
  }

  @Operation(summary = "인기 카테고리 불러오기", description = "인기 카테고리를 불러 올 때 사용하는 API.")
  @ApiResponse(responseCode = "200", description = "인기 카테고리를 불러오는데 성공했습니다.")
  @GetMapping("/favorite-category")
  public ResponseEntity<ResultDTO<List<CategoryDto>>> getFavoriteAuctionCategory() {
    return ResponseEntity.ok(auctionService.getFavoriteAuctionCategory());
  }

  @Operation(summary = "전체 카테고리 불러오기", description = "전체 카테고리를 불러 올 때 사용하는 API.")
  @ApiResponse(responseCode = "200", description = "전체 카테고리를 불러오는데 성공했습니다.")
  @GetMapping("/category")
  public ResponseEntity<ResultDTO<List<CategoryDto>>> getAllAuctionCategory() {
    return ResponseEntity.ok(auctionService.getAllAuctionCategory());
  }

  @Operation(summary = "경매 전체 페이지 불러오기", description = "경매 전체 페이지를 불러 올 때 사용하는 API.")
  @ApiResponse(responseCode = "200", description = "경매 전체 페이지를 불러오는데 성공했습니다.")
  @GetMapping("/list")
  public ResponseEntity<ResultDTO<Page<AuctionDto.AuctionResponse>>> getAllAuctions(
      @Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return ResponseEntity.ok(auctionService.getList(page));
  }

  @Operation(summary = "경매 검색", description = "경매 검색 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "경매 검색을 완료했습니다."),
      @ApiResponse(responseCode = "400", description = "경매 카테고리가 존재하지 않습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/search")
  public ResponseEntity<ResultDTO<Page<AuctionDto.AuctionResponse>>> searchAuctions(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "categoryOption", defaultValue = "ALL") CategoryType category,
      @RequestParam(name = "keyword", defaultValue = "") String keyword) {

    return ResponseEntity.ok(auctionService.getSearchList(page, category, keyword));
  }


  /**
   * 이미지 가져오기
   */
  @GetMapping("/images/auction/{userId}/{filename:.+}")
  public ResponseEntity<Resource> getImage(@PathVariable("userId") String userId,
      @PathVariable("filename") String filename) {
    Path imagePath = basePath.resolve(userId).resolve(filename);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg");

    return ResponseEntity.ok()
        .headers(headers)
        .body(auctionService.getImage(imagePath, headers));
  }

  @Operation(summary = "경매 상세보기", description = "경매 상세 보기 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공적으로 상세보기할 경매가 조회되었습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 경매입니다.<br>존재하지 않는 회원입니다.<br>이미지가 존재하지 않습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/view/{auctionId}")
  public ResultDTO<AuctionDto.AuctionResponse> findAuction(
      @PathVariable(name = "auctionId") Long auctionId) {
    return auctionService.findAuction(auctionId);
  }

  @Operation(summary = "경매 즐겨찾기(찜) 여부 확인", description = "경매 즐겨찾기(찜) 여부 확인 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공적으로 찜 여부가 조회되었습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 경매입니다.<br>존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/favorite/{auctionId}")
  public ResultDTO<AuctionFavoriteDto.Response> favoriteChk(
      @PathVariable(name = "auctionId") Long auctionId) {
    return auctionService.favoriteChk(auctionId);
  }

  @Operation(summary = "경매 즐겨찾기(찜) 토글", description = "경매 즐겨찾기(찜) 하거나 취소 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공적으로 찜 또는 찜취소가 되었습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 경매입니다.<br>존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/favorite/{auctionId}")
  public ResultDTO<AuctionFavoriteDto.Response> favoriteAuction(
      @PathVariable(name = "auctionId") Long auctionId) {
    return auctionService.favoriteAuction(auctionId);
  }

  @Operation(summary = "판매한 경매 목록 불러오기", description = "내가 판매한 경매 목록을 불러 올 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공적으로 판매한 경매 목룍이 조회되었습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/sell-auction-list")
  public ResultDTO<Page<AuctionDto.AuctionResponse>> getSellAuctionList(
      @Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return auctionService.getSellAuctionList(page);
  }

  @Operation(summary = "구매한 경매 목록 불러오기", description = "내가 구매한 경매 목록을 불러 올 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공적으로 구매한 경매 목룍이 조회되었습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/buy-auction-list")
  public ResultDTO<Page<AuctionDto.AuctionResponse>> getBuyAuctionList(
      @Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return auctionService.getBuyAuctionList(page);
  }

  @Operation(summary = "입찰한 경매 목록 불러오기", description = "내가 입찰한 경매 목록을 불러 올 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공적으로 입찰한 경매 목룍이 조회되었습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/join-auction-list")
  public ResultDTO<Page<AuctionDto.AuctionResponse>> getJoinAuctionList(
      @Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return auctionService.getJoinAuctionList(page);
  }

  @Operation(summary = "즐겨찾기(찜)한 경매 목록 불러오기", description = "내가 즐겨찾기(찜)한 경매 목록을 불러 올 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공적으로 찜한 경매 목룍이 조회되었습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/favorite-auction-list")
  public ResultDTO<Page<AuctionDto.AuctionResponse>> getFavoriteAuctionList(
      @Valid @RequestParam(value = "page", defaultValue = "0") int page) {
    return auctionService.getFavoriteAuctionList(page);
  }

  @Operation(summary = "경매 신고", description = "경매 신고 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공적으로 경매신고가 완료되었습니다."),
      @ApiResponse(responseCode = "400", description = "이미 신고한 경매 입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.<bt>존재하지 않는 경매입니다",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/report/{auctionId}")
  public ResponseEntity<ResultDTO<AuctionReportDto.AuctionReportResponse>> reportAuction(@PathVariable Long auctionId,
      @RequestBody AuctionReportDto.AuctionReportRequest request) {
    ResultDTO<AuctionReportResponse> result = auctionService.reportAuction(
        auctionId, request);
    return ResponseEntity.ok(result);
  }
}
